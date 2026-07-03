import React from 'react';
import { ShieldCheck, Cpu, ArrowUpRight } from 'lucide-react';

const Navbar = () => {
  return (
    <nav className="w-full px-6 lg:px-[120px] py-6 flex items-center justify-between bg-transparent border-b border-white/5 relative z-50">
      {/* Brand Logo */}
      <div className="flex items-center gap-2 select-none group">
        <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-purple-600 to-indigo-600 flex items-center justify-center shadow-lg shadow-purple-900/30 group-hover:scale-105 transition-transform duration-300">
          <ShieldCheck className="w-6 h-6 text-white" />
        </div>
        <div className="flex flex-col">
          <span className="font-fustat font-bold text-white text-lg tracking-wide leading-none">Antigravity</span>
          <span className="font-inter text-xs text-purple-400 font-semibold tracking-wider uppercase mt-1">CV SCREENER</span>
        </div>
      </div>

      {/* Navigation Links */}
      <div className="hidden md:flex items-center gap-8 font-inter text-sm font-medium text-gray-400">
        <a href="#features" className="hover:text-white transition-colors duration-200 cursor-pointer">Features</a>
        <a href="#architecture" className="hover:text-white transition-colors duration-200 cursor-pointer flex items-center gap-1">
          Architecture <Cpu className="w-3.5 h-3.5 text-gray-500" />
        </a>
        <a href="file:///D:/Project_thucTap/_bmad-output/planning-artifacts/prds/prd-Project_thucTap-2026-07-01/prd.md" className="hover:text-white transition-colors duration-200 cursor-pointer">Technical PRD</a>
        <a href="https://github.com" target="_blank" rel="noreferrer" className="hover:text-white transition-colors duration-200 cursor-pointer">GitHub</a>
      </div>

      {/* Action Button */}
      <div className="flex items-center">
        <button className="relative group px-5 py-2.5 rounded-xl font-fustat font-semibold text-sm text-white overflow-hidden bg-white/5 border border-white/10 hover:border-purple-500/30 hover:bg-purple-950/20 transition-all duration-300">
          <span className="relative z-10 flex items-center gap-1.5">
            Dashboard
            <ArrowUpRight className="w-4 h-4 text-purple-400 group-hover:translate-x-0.5 group-hover:-translate-y-0.5 transition-transform duration-200" />
          </span>
          <div className="absolute inset-0 -z-10 bg-gradient-to-r from-purple-600/0 via-purple-600/10 to-indigo-600/0 opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
        </button>
      </div>
    </nav>
  );
};

export default Navbar;
