import { useRef, useMemo } from 'react';
import { Canvas, useFrame } from '@react-three/fiber';
import * as THREE from 'three';

/* ─── Reactive cube with edge glow ─── */
function Cube({ mouse }) {
  const groupRef = useRef();
  const edgesRef = useRef();
  const innerRef = useRef();
  const targetRot = useRef({ x: 0, y: 0 });

  useFrame((state, delta) => {
    if (!groupRef.current) return;
    const t = state.clock.elapsedTime;

    // Smooth follow cursor
    targetRot.current.x += (mouse.current.y * 0.8 - targetRot.current.x) * delta * 2.5;
    targetRot.current.y += (mouse.current.x * 1.0 - targetRot.current.y) * delta * 2.5;

    groupRef.current.rotation.x = targetRot.current.x + Math.sin(t * 0.3) * 0.08;
    groupRef.current.rotation.y = targetRot.current.y + t * 0.06;
    groupRef.current.rotation.z = Math.sin(t * 0.2) * 0.05;

    // Breathing
    const s = 1.35 + Math.sin(t * 0.7) * 0.04;
    groupRef.current.scale.setScalar(s);

    // Pulsing edge opacity
    if (edgesRef.current) {
      edgesRef.current.material.opacity = 0.45 + Math.sin(t * 1.5) * 0.15;
    }
    // Inner cube counter-rotates slightly
    if (innerRef.current) {
      innerRef.current.rotation.x = -targetRot.current.x * 0.3;
      innerRef.current.rotation.y = -targetRot.current.y * 0.3 + t * 0.1;
    }
  });

  const edgesGeo = useMemo(() => {
    const box = new THREE.BoxGeometry(1, 1, 1);
    return new THREE.EdgesGeometry(box);
  }, []);

  const innerEdgesGeo = useMemo(() => {
    const box = new THREE.BoxGeometry(1, 1, 1);
    return new THREE.EdgesGeometry(box);
  }, []);

  return (
    <group ref={groupRef}>
      {/* Outer solid faces — very transparent */}
      <mesh>
        <boxGeometry args={[1, 1, 1]} />
        <meshPhysicalMaterial
          color="#73eedc"
          metalness={0.05}
          roughness={0.2}
          transparent
          opacity={0.08}
          side={THREE.DoubleSide}
          depthWrite={false}
        />
      </mesh>

      {/* Outer edges — bright turquoise lines */}
      <lineSegments ref={edgesRef} geometry={edgesGeo}>
        <lineBasicMaterial color="#73eedc" transparent opacity={0.55} linewidth={1} />
      </lineSegments>

      {/* Inner cube — smaller, counter-rotates */}
      <group ref={innerRef}>
        <mesh scale={0.55}>
          <boxGeometry args={[1, 1, 1]} />
          <meshPhysicalMaterial
            color="#73c2be"
            metalness={0.15}
            roughness={0.1}
            transparent
            opacity={0.18}
            side={THREE.DoubleSide}
            depthWrite={false}
          />
        </mesh>
        <lineSegments geometry={innerEdgesGeo} scale={0.55}>
          <lineBasicMaterial color="#73c2be" transparent opacity={0.35} linewidth={1} />
        </lineSegments>
      </group>

      {/* Corner dots */}
      <CornerDots />
    </group>
  );
}

/* ─── Dots at each corner of the cube ─── */
function CornerDots() {
  const positions = useMemo(() => {
    const corners = [];
    for (let x = -1; x <= 1; x += 2)
      for (let y = -1; y <= 1; y += 2)
        for (let z = -1; z <= 1; z += 2)
          corners.push(x * 0.5, y * 0.5, z * 0.5);
    return new Float32Array(corners);
  }, []);

  return (
    <points>
      <bufferGeometry>
        <bufferAttribute attach="attributes-position" count={8} array={positions} itemSize={3} />
      </bufferGeometry>
      <pointsMaterial size={0.06} color="#73eedc" transparent opacity={0.8} sizeAttenuation depthWrite={false} />
    </points>
  );
}

/* ─── Orbit particles ─── */
function Particles({ count = 50, mouse }) {
  const ref = useRef();

  const positions = useMemo(() => {
    const pos = new Float32Array(count * 3);
    for (let i = 0; i < count; i++) {
      // Distribute along cube edges and corners area
      const theta = Math.random() * Math.PI * 2;
      const phi = Math.acos(2 * Math.random() - 1);
      const r = 1.6 + Math.random() * 1.2;
      pos[i * 3] = r * Math.sin(phi) * Math.cos(theta);
      pos[i * 3 + 1] = r * Math.sin(phi) * Math.sin(theta);
      pos[i * 3 + 2] = r * Math.cos(phi);
    }
    return pos;
  }, [count]);

  useFrame((state, delta) => {
    if (!ref.current) return;
    ref.current.rotation.y += delta * 0.04 + mouse.current.x * delta * 0.06;
    ref.current.rotation.x += mouse.current.y * delta * 0.03;
  });

  return (
    <points ref={ref}>
      <bufferGeometry>
        <bufferAttribute attach="attributes-position" count={count} array={positions} itemSize={3} />
      </bufferGeometry>
      <pointsMaterial size={0.025} color="#73eedc" transparent opacity={0.4} sizeAttenuation depthWrite={false} />
    </points>
  );
}

/* ─── Floating grid lines around the cube ─── */
function GridLines({ mouse }) {
  const ref = useRef();

  useFrame((state, delta) => {
    if (!ref.current) return;
    ref.current.rotation.y = mouse.current.x * 0.15;
    ref.current.rotation.x = mouse.current.y * 0.1 + Math.PI / 2;
    ref.current.rotation.z += delta * 0.02;
  });

  const geo = useMemo(() => {
    const g = new THREE.BufferGeometry();
    const verts = [];
    const size = 2.8;
    const divs = 6;
    const step = size / divs;
    for (let i = 0; i <= divs; i++) {
      const p = -size / 2 + i * step;
      verts.push(-size / 2, 0, p, size / 2, 0, p);
      verts.push(p, 0, -size / 2, p, 0, size / 2);
    }
    g.setAttribute('position', new THREE.Float32BufferAttribute(verts, 3));
    return g;
  }, []);

  return (
    <lineSegments ref={ref} geometry={geo}>
      <lineBasicMaterial color="#73eedc" transparent opacity={0.06} depthWrite={false} />
    </lineSegments>
  );
}

/* ─── Mouse tracker inside canvas ─── */
function MouseTracker({ mouse }) {
  useFrame(({ pointer }) => {
    mouse.current.x = pointer.x;
    mouse.current.y = pointer.y;
  });
  return null;
}

/* ─── Main export ─── */
export default function HeroScene() {
  const mouse = useRef({ x: 0, y: 0 });

  return (
    <div className="absolute inset-0" style={{ cursor: 'grab' }}>
      <Canvas
        camera={{ position: [0, 0, 5], fov: 40 }}
        dpr={[1, 1.5]}
        gl={{ antialias: true, alpha: true, powerPreference: 'high-performance' }}
        style={{ background: 'transparent' }}
        onCreated={({ gl }) => {
          gl.setClearColor(0x000000, 0);
          gl.toneMapping = THREE.NoToneMapping;
        }}
      >
        <ambientLight intensity={0.4} />
        <directionalLight position={[5, 5, 5]} intensity={0.9} color="#ffffff" />
        <directionalLight position={[-3, -2, 4]} intensity={0.3} color="#73eedc" />
        <pointLight position={[0, 3, 2]} intensity={0.5} color="#73c2be" />

        <MouseTracker mouse={mouse} />
        <Cube mouse={mouse} />
        <Particles mouse={mouse} />
        <GridLines mouse={mouse} />
      </Canvas>
    </div>
  );
}
