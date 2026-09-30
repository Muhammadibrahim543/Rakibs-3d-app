import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';
import { TransformControls } from 'three/examples/jsm/controls/TransformControls.js';
import { SUBTRACTION, ADDITION, Brush, Evaluator } from 'three-bvh-csg';
import { STLLoader } from 'three/examples/jsm/loaders/STLLoader.js';
import { OBJLoader } from 'three/examples/jsm/loaders/OBJLoader.js';
import { STLExporter } from 'three/examples/jsm/exporters/STLExporter.js';

// Core setup
const container = document.getElementById('viewport');
const scene = new THREE.Scene();
scene.background = new THREE.Color(0x111111);

// Grid and Lights
const gridHelper = new THREE.GridHelper(100, 100, 0x444444, 0x222222);
scene.add(gridHelper);

const ambientLight = new THREE.AmbientLight(0xffffff, 0.6);
scene.add(ambientLight);
const dirLight = new THREE.DirectionalLight(0xffffff, 0.8);
dirLight.position.set(10, 20, 10);
scene.add(dirLight);

const camera = new THREE.PerspectiveCamera(50, container.clientWidth / container.clientHeight, 0.1, 1000);
camera.position.set(30, 30, 30);
camera.lookAt(0, 0, 0);

const renderer = new THREE.WebGLRenderer({ antialias: true });
renderer.setSize(container.clientWidth, container.clientHeight);
renderer.setPixelRatio(window.devicePixelRatio);
container.appendChild(renderer.domElement);

const orbit = new OrbitControls(camera, renderer.domElement);
const transformControl = new TransformControls(camera, renderer.domElement);
scene.add(transformControl);

transformControl.addEventListener('dragging-changed', function (event) {
    orbit.enabled = !event.value;
});
transformControl.addEventListener('change', function () {
    if(selectedObjects.length === 1) updatePropertiesPanel(selectedObjects[0]);
});

// View Cube Camera Gizmo (Simple implementation: just top/front/right buttons mapped to camera positions)
function setupViewCube() {
    const vc = document.getElementById('viewcube');
    vc.style.pointerEvents = 'auto';
    vc.style.display = 'flex';
    vc.style.flexDirection = 'column';
    vc.style.gap = '2px';
    
    const views = {
        'Top': [0, 50, 0],
        'Front': [0, 0, 50],
        'Right': [50, 0, 0],
        'Iso': [30, 30, 30]
    };
    
    for(const [name, pos] of Object.entries(views)) {
        const b = document.createElement('button');
        b.innerText = name;
        b.style.background = '#333';
        b.style.color = 'white';
        b.style.border = '1px solid #555';
        b.onclick = () => {
            camera.position.set(pos[0], pos[1], pos[2]);
            camera.lookAt(0,0,0);
            orbit.target.set(0,0,0);
            orbit.update();
        };
        vc.appendChild(b);
    }
}
setupViewCube();

window.addEventListener('resize', () => {
    camera.aspect = container.clientWidth / container.clientHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(container.clientWidth, container.clientHeight);
});

// Selection & Raycasting
const raycaster = new THREE.Raycaster();
const mouse = new THREE.Vector2();
let selectedObjects = [];
const objects = []; 

// Materials
const solidMaterial = new THREE.MeshStandardMaterial({ color: 0x4285F4, roughness: 0.4 });
const holeMaterial = new THREE.MeshStandardMaterial({ 
    color: 0xffaa00, 
    transparent: true, 
    opacity: 0.5,
    wireframe: true 
});

function addShape(type) {
    let geometry;
    if (type === 'box') geometry = new THREE.BoxGeometry(10, 10, 10);
    else if (type === 'cylinder') geometry = new THREE.CylinderGeometry(5, 5, 10, 32);
    else if (type === 'sphere') geometry = new THREE.SphereGeometry(5, 32, 32);
    else if (type === 'cone') geometry = new THREE.ConeGeometry(5, 10, 32);
    else return;

    const mesh = new THREE.Mesh(geometry, solidMaterial.clone());
    mesh.position.y = 5;
    mesh.userData = { isHole: false, isGroup: false };
    
    scene.add(mesh);
    objects.push(mesh);
    selectObject(mesh);
}

document.querySelectorAll('.shape-btn').forEach(btn => {
    btn.addEventListener('click', () => addShape(btn.dataset.shape));
});

// Interaction
container.addEventListener('pointerdown', (e) => {
    if(e.button !== 0) return; 
    if(transformControl.dragging) return; 

    mouse.x = ((e.clientX - container.getBoundingClientRect().left) / container.clientWidth) * 2 - 1;
    mouse.y = -((e.clientY - container.getBoundingClientRect().top) / container.clientHeight) * 2 + 1;

    raycaster.setFromCamera(mouse, camera);
    const intersects = raycaster.intersectObjects(objects, false);

    if (intersects.length > 0) {
        if (e.shiftKey) { 
            const obj = intersects[0].object;
            if (selectedObjects.includes(obj)) deselectObject(obj);
            else selectObject(obj, true);
        } else {
            clearSelection();
            selectObject(intersects[0].object);
        }
    } else {
        clearSelection();
    }
});

const boxHelper = new THREE.BoxHelper(new THREE.Object3D(), 0xffff00);
scene.add(boxHelper);
boxHelper.visible = false;

function selectObject(obj, multi = false) {
    if (!multi) clearSelection();
    if (!selectedObjects.includes(obj)) selectedObjects.push(obj);
    updateSelectionUI();
}

function deselectObject(obj) {
    selectedObjects = selectedObjects.filter(o => o !== obj);
    updateSelectionUI();
}

function clearSelection() {
    selectedObjects = [];
    transformControl.detach();
    boxHelper.visible = false;
    updateSelectionUI();
}

function updateSelectionUI() {
    if (selectedObjects.length === 0) {
        transformControl.detach();
        boxHelper.visible = false;
        document.getElementById('panel-properties').style.display = 'none';
        document.getElementById('panel-shapes').style.display = 'block';
        document.getElementById('measurement-overlay').innerText = "";
    } else if (selectedObjects.length === 1) {
        transformControl.attach(selectedObjects[0]);
        boxHelper.setFromObject(selectedObjects[0]);
        boxHelper.visible = true;
        
        document.getElementById('panel-properties').style.display = 'block';
        document.getElementById('panel-shapes').style.display = 'none';
        
        updatePropertiesPanel(selectedObjects[0]);
    } else {
        transformControl.detach(); 
        document.getElementById('panel-properties').style.display = 'none';
        document.getElementById('panel-shapes').style.display = 'none';
    }
}

function updatePropertiesPanel(obj) {
    const isHole = obj.userData.isHole;
    document.getElementById('toggle-solid').classList.toggle('selected', !isHole);
    document.getElementById('toggle-hole').classList.toggle('selected', isHole);
    
    document.getElementById('pos-x').value = obj.position.x.toFixed(1);
    document.getElementById('pos-y').value = obj.position.z.toFixed(1);
    document.getElementById('pos-z').value = obj.position.y.toFixed(1);
    
    // Ruler overlay updates
    const box = new THREE.Box3().setFromObject(obj);
    const size = new THREE.Vector3();
    box.getSize(size);
    document.getElementById('dim-x').value = size.x.toFixed(1);
    document.getElementById('dim-y').value = size.z.toFixed(1);
    document.getElementById('dim-z').value = size.y.toFixed(1);
    
    document.getElementById('measurement-overlay').innerText = `W:${size.x.toFixed(1)} H:${size.y.toFixed(1)} D:${size.z.toFixed(1)}`;
}

// Axis Input UI Binding
document.getElementById('pos-x').addEventListener('change', (e) => { if(selectedObjects.length===1) selectedObjects[0].position.x = parseFloat(e.target.value); });
document.getElementById('pos-y').addEventListener('change', (e) => { if(selectedObjects.length===1) selectedObjects[0].position.z = parseFloat(e.target.value); });
document.getElementById('pos-z').addEventListener('change', (e) => { if(selectedObjects.length===1) selectedObjects[0].position.y = parseFloat(e.target.value); });

// Toggle Solid/Hole
document.getElementById('toggle-solid').addEventListener('click', () => {
    if(selectedObjects.length === 1) {
        selectedObjects[0].userData.isHole = false;
        selectedObjects[0].material = solidMaterial.clone();
        updatePropertiesPanel(selectedObjects[0]);
    }
});
document.getElementById('toggle-hole').addEventListener('click', () => {
    if(selectedObjects.length === 1) {
        selectedObjects[0].userData.isHole = true;
        selectedObjects[0].material = holeMaterial.clone();
        updatePropertiesPanel(selectedObjects[0]);
    }
});

// Group / CSG Evaluate
const evaluator = new Evaluator();
evaluator.useGroups = true;

document.getElementById('btn-group').addEventListener('click', () => {
    if (selectedObjects.length < 2) return;
    
    const solids = selectedObjects.filter(o => !o.userData.isHole);
    const holes = selectedObjects.filter(o => o.userData.isHole);
    if (solids.length === 0) return;
    
    let resultBrush = new Brush(solids[0].geometry, solids[0].material);
    resultBrush.position.copy(solids[0].position);
    resultBrush.rotation.copy(solids[0].rotation);
    resultBrush.scale.copy(solids[0].scale);
    resultBrush.updateMatrixWorld();

    for(let i=1; i<solids.length; i++) {
        let b = new Brush(solids[i].geometry, solids[i].material);
        b.position.copy(solids[i].position);
        b.rotation.copy(solids[i].rotation);
        b.scale.copy(solids[i].scale);
        b.updateMatrixWorld();
        resultBrush = evaluator.evaluate(resultBrush, b, ADDITION);
    }
    
    for(let i=0; i<holes.length; i++) {
        let b = new Brush(holes[i].geometry, holes[i].material);
        b.position.copy(holes[i].position);
        b.rotation.copy(holes[i].rotation);
        b.scale.copy(holes[i].scale);
        b.updateMatrixWorld();
        resultBrush = evaluator.evaluate(resultBrush, b, SUBTRACTION);
    }

    const finalMesh = new THREE.Mesh(resultBrush.geometry, resultBrush.material);
    finalMesh.userData = { isHole: false, isGroup: true, originalObjects: selectedObjects.slice() };
    
    selectedObjects.forEach(obj => {
        scene.remove(obj);
        objects.splice(objects.indexOf(obj), 1);
    });
    
    scene.add(finalMesh);
    objects.push(finalMesh);
    clearSelection();
    selectObject(finalMesh);
});

// Ungroup
document.getElementById('btn-ungroup').addEventListener('click', () => {
    if (selectedObjects.length !== 1 || !selectedObjects[0].userData.isGroup) return;
    
    const groupMesh = selectedObjects[0];
    const originals = groupMesh.userData.originalObjects;
    
    scene.remove(groupMesh);
    objects.splice(objects.indexOf(groupMesh), 1);
    
    originals.forEach(obj => {
        scene.add(obj);
        objects.push(obj);
    });
    
    clearSelection();
});

// Delete
document.getElementById('btn-delete').addEventListener('click', () => {
    selectedObjects.forEach(obj => {
        scene.remove(obj);
        objects.splice(objects.indexOf(obj), 1);
    });
    clearSelection();
});

// Export (Basic STL export text format wrapper for the Java console.log logic)
document.getElementById('btn-export').addEventListener('click', () => {
    const exporter = new STLExporter();
    const result = exporter.parse(scene);
    console.log("STLFILE---" + result);
});

// Align Tool (Basic snapping center)
document.getElementById('btn-align').addEventListener('click', () => {
    if (selectedObjects.length < 2) return;
    
    const box = new THREE.Box3();
    selectedObjects.forEach(obj => box.expandByObject(obj));
    
    const center = new THREE.Vector3();
    box.getCenter(center);
    
    selectedObjects.forEach(obj => {
        obj.position.x = center.x;
        obj.position.z = center.z;
    });
});

// Render Loop
function animate() {
    requestAnimationFrame(animate);
    
    if (selectedObjects.length === 1) {
        boxHelper.update();
        
        // update measurement overlay position
        const vector = new THREE.Vector3();
        boxHelper.geometry.computeBoundingBox();
        boxHelper.geometry.boundingBox.getCenter(vector);
        vector.y = boxHelper.geometry.boundingBox.max.y;
        
        vector.project(camera);
        
        const x = (vector.x * .5 + .5) * container.clientWidth;
        const y = (vector.y * -.5 + .5) * container.clientHeight;
        
        const mo = document.getElementById('measurement-overlay');
        mo.style.left = `${x}px`;
        mo.style.top = `${y - 20}px`;
    }
    
    renderer.render(scene, camera);
}
animate();
