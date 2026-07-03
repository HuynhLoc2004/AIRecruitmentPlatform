import React, { useRef, useEffect } from 'react';

const VideoBackground = ({ videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-abstract-dark-blue-and-purple-flow-background-43224-large.mp4" }) => {
  const videoRef = useRef(null);
  const fadingOutRef = useRef(false);
  const fadingInRef = useRef(false);

  const fadeIn = () => {
    if (fadingInRef.current) return;
    fadingInRef.current = true;
    let startTime = null;

    const animate = (timestamp) => {
      if (!startTime) startTime = timestamp;
      const elapsed = timestamp - startTime;
      const opacity = Math.min(elapsed / 250, 1); // 250ms fade-in

      if (videoRef.current) {
        videoRef.current.style.opacity = opacity;
      }

      if (elapsed < 250) {
        requestAnimationFrame(animate);
      } else {
        fadingInRef.current = false;
      }
    };

    requestAnimationFrame(animate);
  };

  const fadeOut = () => {
    let startTime = null;

    const animate = (timestamp) => {
      if (!startTime) startTime = timestamp;
      const elapsed = timestamp - startTime;
      const opacity = Math.max(1 - (elapsed / 250), 0); // 250ms fade-out

      if (videoRef.current) {
        videoRef.current.style.opacity = opacity;
      }

      if (elapsed < 250) {
        requestAnimationFrame(animate);
      }
    };

    requestAnimationFrame(animate);
  };

  const handleTimeUpdate = () => {
    const video = videoRef.current;
    if (!video) return;

    const timeLeft = video.duration - video.currentTime;

    // Trigger fade-out when 0.55s remain
    if (timeLeft <= 0.55 && !fadingOutRef.current && video.duration > 0) {
      fadingOutRef.current = true;
      fadeOut();
    }

    // Reset loop boundaries to trigger fade-in again when video loops back to start
    if (video.currentTime < 0.1 && fadingOutRef.current) {
      fadingOutRef.current = false;
      fadeIn();
    }
  };

  const handleCanPlay = () => {
    fadeIn();
  };

  useEffect(() => {
    const video = videoRef.current;
    if (video) {
      video.style.opacity = 0; // Initialize hidden
    }
  }, []);

  return (
    <div className="absolute inset-0 w-full h-full overflow-hidden -z-10 bg-black">
      {/* Dark overlay to ensure text readability */}
      <div className="absolute inset-0 bg-black/60 z-10" />
      <video
        ref={videoRef}
        src={videoUrl}
        autoPlay
        muted
        loop
        playsInline
        onTimeUpdate={handleTimeUpdate}
        onCanPlay={handleCanPlay}
        className="w-full h-full object-cover select-none pointer-events-none"
        style={{
          opacity: 0,
          willChange: 'opacity',
        }}
      />
    </div>
  );
};

export default VideoBackground;
