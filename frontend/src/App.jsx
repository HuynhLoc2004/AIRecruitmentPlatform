import React from 'react';
import Navbar from './components/Navbar';
import HeroContent from './components/HeroContent';
import VideoBackground from './components/VideoBackground';
import './App.css';

function App() {
  return (
    <div className="min-h-screen w-screen bg-[#030008] text-white relative overflow-x-hidden flex flex-col justify-start">
      {/* Absolute Cinematic Video Background */}
      <VideoBackground />

      {/* Navigation Header */}
      <Navbar />

      {/* Main Hero & Search area */}
      <main className="flex-grow flex items-center justify-center">
        <HeroContent />
      </main>

      {/* Decorative subtle ambient glows */}
      <div className="absolute bottom-0 left-0 w-[500px] h-[300px] bg-indigo-900/10 rounded-full blur-[120px] pointer-events-none -z-10" />
      <div className="absolute top-0 right-0 w-[400px] h-[400px] bg-purple-950/10 rounded-full blur-[100px] pointer-events-none -z-10" />
    </div>
  );
}

export default App;
