import React, { useEffect, useRef, useState } from 'react';
import { Paperclip, Mic, MicOff, Sparkles, Send, FileText, CheckCircle2, AlertCircle, Mail, Phone, GraduationCap, X } from 'lucide-react';
import api from '../services/api';

const HeroContent = () => {
  const [inputText, setInputText] = useState("");
  const [attachedFile, setAttachedFile] = useState(null);
  
  // States for API interactions
  const [isLoading, setIsLoading] = useState(false);
  const [uploadStatus, setUploadStatus] = useState("idle"); // idle, uploading, success, error
  const [searchResults, setSearchResults] = useState([]);
  const [hasSearched, setHasSearched] = useState(false);
  const [isListening, setIsListening] = useState(false);
  const [voiceSupported, setVoiceSupported] = useState(true);

  // Toast / alert feedback state
  const [toast, setToast] = useState({ visible: false, message: "", type: "info" });
  const fileInputRef = useRef(null);
  const recognitionRef = useRef(null);

  useEffect(() => {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SpeechRecognition) {
      setVoiceSupported(false);
      return;
    }

    const recognition = new SpeechRecognition();
    recognition.lang = "vi-VN";
    recognition.continuous = false;
    recognition.interimResults = false;

    recognition.onresult = (event) => {
      let transcript = "";
      for (let i = event.resultIndex; i < event.results.length; i++) {
        transcript += event.results[i][0].transcript;
      }

      if (transcript.trim()) {
        setInputText(prev => {
          const separator = prev.trim() ? " " : "";
          return `${prev}${separator}${transcript.trim()}`.slice(0, 2000);
        });
      }
    };

    recognition.onerror = (event) => {
      setIsListening(false);
      showToast(`Voice input stopped: ${event.error}`, "error");
    };

    recognition.onend = () => {
      setIsListening(false);
    };

    recognitionRef.current = recognition;

    return () => {
      recognition.stop();
    };
  }, []);

  const showToast = (message, type = "success") => {
    setToast({ visible: true, message, type });
    setTimeout(() => {
      setToast(prev => ({ ...prev, visible: false }));
    }, 4000);
  };

  const handleInputChange = (e) => {
    if (e.target.value.length <= 2000) {
      setInputText(e.target.value);
    }
  };

  const handleAttachClick = () => {
    fileInputRef.current.click();
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      const extension = file.name.split('.').pop().toLowerCase();
      if (extension !== 'pdf' && extension !== 'docx') {
        showToast("Unsupported format. Please upload PDF or DOCX.", "error");
        return;
      }
      setAttachedFile(file);
      setUploadStatus("idle");
      showToast(`Selected file: ${file.name}. Click Send to upload.`, "info");
    }
  };

  const clearAttachedFile = () => {
    setAttachedFile(null);
    setUploadStatus("idle");
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  const handleVoiceInput = () => {
    if (attachedFile) {
      showToast("Remove the attached CV before using voice search.", "info");
      return;
    }

    if (!voiceSupported || !recognitionRef.current) {
      showToast("Voice input is not supported in this browser. Try Chrome or Edge.", "error");
      return;
    }

    if (isListening) {
      recognitionRef.current.stop();
      setIsListening(false);
      return;
    }

    try {
      recognitionRef.current.start();
      setIsListening(true);
      showToast("Listening... speak your candidate search query.", "info");
    } catch (error) {
      logMessage("Voice start error: " + error);
      setIsListening(false);
      showToast("Voice input is already starting. Please try again.", "error");
    }
  };

  const handleSend = async () => {
    if (isLoading) return;

    // 1. Action: CV File Upload Ingestion
    if (attachedFile) {
      setIsLoading(true);
      setUploadStatus("uploading");
      try {
        logMessage("Uploading resume file...");
        const response = await api.uploadCv(attachedFile);
        
        setUploadStatus("success");
        showToast("Resume successfully enqueued (202 Accepted)!", "success");
        logMessage("Upload succeeded: " + JSON.stringify(response));
        
        // Keep file attached but marked success
      } catch (error) {
        setUploadStatus("error");
        showToast("Failed to upload CV: Server error.", "error");
        logMessage("Upload error: " + error);
      } finally {
        setIsLoading(false);
      }
      return;
    }

    // 2. Action: Semantic Candidate Search
    if (inputText.trim()) {
      setIsLoading(true);
      setHasSearched(true);
      try {
        logMessage("Executing candidate search query...");
        const results = await api.searchCandidates(inputText);
        setSearchResults(results);
        showToast(`Search completed. Found ${results.length} matching candidates.`, "success");
      } catch (error) {
        showToast("Failed to search candidates. Make sure database is running.", "error");
        logMessage("Search error: " + error);
        setSearchResults([]);
      } finally {
        setIsLoading(false);
      }
    }
  };

  const logMessage = (msg) => {
    console.log("[HeroContent] " + msg);
  };

  return (
    <div className="w-full px-6 lg:px-[120px] pt-16 pb-24 flex flex-col items-center text-center relative z-20 -mt-[50px]">
      
      {/* Toast Alert Feedback */}
      {toast.visible && (
        <div className={`fixed top-6 right-6 z-50 flex items-center gap-2 px-4 py-3 rounded-xl border backdrop-blur-md transition-all duration-300 shadow-2xl ${
          toast.type === "success" ? "bg-emerald-950/40 border-emerald-500/20 text-emerald-300" :
          toast.type === "error" ? "bg-rose-950/40 border-rose-500/20 text-rose-300" :
          "bg-purple-950/40 border-purple-500/20 text-purple-300"
        }`}>
          {toast.type === "success" ? <CheckCircle2 className="w-4 h-4" /> : <AlertCircle className="w-4 h-4" />}
          <span className="font-inter text-xs font-semibold">{toast.message}</span>
        </div>
      )}

      {/* Glow effect under the hero */}
      <div className="absolute top-20 left-1/2 -translate-x-1/2 w-[350px] h-[350px] rounded-full bg-purple-600/10 blur-[100px] pointer-events-none" />

      {/* Glassmorphic Tech Badge */}
      <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full border border-purple-500/20 bg-purple-500/5 hover:border-purple-500/30 transition-all duration-300 select-none cursor-default mb-8">
        <Sparkles className="w-3.5 h-3.5 text-purple-400 animate-pulse" />
        <span className="font-fustat text-xs text-purple-200 font-semibold tracking-wide uppercase">
          AI CV screening pipeline is active
        </span>
      </div>

      {/* Main Title */}
      <h1 className="font-fustat font-extrabold text-4xl sm:text-5xl md:text-6xl lg:text-7xl tracking-tight text-white max-w-4xl leading-[1.1] mb-6">
        Map Candidates to Job Descriptions{' '}
        <span className="text-transparent bg-clip-text bg-gradient-to-r from-purple-400 via-purple-500 to-indigo-400">
          Semantically
        </span>
      </h1>

      {/* Subtitle */}
      <p className="font-inter text-gray-400 text-sm sm:text-base lg:text-lg max-w-2xl leading-relaxed mb-12">
        Ingest bulk PDF/DOCX resumes, extract profiles using structured LLMs, and perform instant vector-indexed match queries backed by Spring Boot, RabbitMQ backpressure queues, and pgvector.
      </p>

      {/* Complex LLM-Style Search Ingest Box */}
      <div className="w-full max-w-2xl rounded-2xl border border-white/10 bg-black/45 backdrop-blur-md shadow-2xl shadow-purple-900/10 p-4 flex flex-col gap-4 focus-within:border-purple-500/30 transition-colors duration-300">
        
        {/* Hidden File Input */}
        <input 
          type="file" 
          ref={fileInputRef} 
          onChange={handleFileChange} 
          className="hidden" 
          accept=".pdf,.docx" 
        />

        {/* Input Text Area */}
        <div className="flex gap-3 items-start">
          <textarea
            value={inputText}
            onChange={handleInputChange}
            placeholder={attachedFile ? `Resume file attached. Click Send to submit.` : "Describe candidate requirements (e.g. 'Senior Java developer with Kafka and Redis experience')..."}
            disabled={attachedFile !== null}
            className="w-full bg-transparent text-white font-inter text-sm outline-none resize-none h-20 placeholder-gray-500 py-1 disabled:opacity-50"
          />
          
          {/* Send / Upload Button */}
          <button 
            onClick={handleSend}
            disabled={isLoading || (!inputText.trim() && !attachedFile)}
            className="w-10 h-10 rounded-full bg-gradient-to-tr from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 flex items-center justify-center text-white shadow-lg shadow-purple-900/40 hover:scale-105 disabled:opacity-50 disabled:scale-100 disabled:pointer-events-none transition-all duration-200"
          >
            {isLoading ? (
              <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              <Send className="w-4 h-4" />
            )}
          </button>
        </div>

        {/* Display Uploading / Attached File Status */}
        {attachedFile && (
          <div className="flex items-center justify-between px-3 py-2 rounded-xl bg-white/5 border border-white/5 font-inter text-xs">
            <div className="flex items-center gap-2">
              <FileText className="w-4 h-4 text-purple-400" />
              <span className="text-gray-200 truncate max-w-[200px] md:max-w-xs">{attachedFile.name}</span>
            </div>
            
            <div className="flex items-center gap-3">
              <div className="flex items-center gap-1.5 text-gray-400">
                {uploadStatus === "idle" && <span className="text-purple-400">Ready to Ingest</span>}
                {uploadStatus === "uploading" && (
                  <div className="flex items-center gap-1.5">
                    <div className="w-3 h-3 border-2 border-purple-500 border-t-transparent rounded-full animate-spin" />
                    <span>Uploading...</span>
                  </div>
                )}
                {uploadStatus === "success" && (
                  <div className="flex items-center gap-1.5 text-emerald-400">
                    <CheckCircle2 className="w-3.5 h-3.5" />
                    <span>Enqueued (202 Accepted)</span>
                  </div>
                )}
                {uploadStatus === "error" && (
                  <div className="flex items-center gap-1.5 text-rose-400">
                    <AlertCircle className="w-3.5 h-3.5" />
                    <span>Upload Failed</span>
                  </div>
                )}
              </div>
              
              {!isLoading && (
                <button 
                  onClick={clearAttachedFile}
                  className="p-1 rounded-md text-gray-500 hover:text-white hover:bg-white/5 transition-colors"
                >
                  <X className="w-3.5 h-3.5" />
                </button>
              )}
            </div>
          </div>
        )}

        {/* Footer actions & character count */}
        <div className="flex items-center justify-between border-t border-white/5 pt-3">
          {/* Action buttons (Attach, Voice, Prompts) */}
          <div className="flex items-center gap-2">
            
            {/* Attach button */}
            <button 
              onClick={handleAttachClick}
              disabled={isLoading}
              className="p-2 rounded-lg bg-white/5 hover:bg-white/10 hover:text-white text-gray-400 transition-colors duration-200 flex items-center gap-1.5 text-xs font-medium font-inter disabled:opacity-50 disabled:pointer-events-none"
            >
              <Paperclip className="w-3.5 h-3.5" />
              Attach CV
            </button>

            {/* Voice button */}
            <button 
              onClick={handleVoiceInput}
              disabled={isLoading || attachedFile !== null || !voiceSupported}
              className={`p-2 rounded-lg transition-colors duration-200 flex items-center gap-1.5 text-xs font-medium font-inter disabled:opacity-50 disabled:pointer-events-none ${
                isListening
                  ? "bg-rose-500/15 text-rose-300 border border-rose-500/20"
                  : "bg-white/5 hover:bg-white/10 hover:text-white text-gray-400"
              }`}
              title={voiceSupported ? "Speak a search query" : "Voice input is not supported in this browser"}
            >
              {isListening ? <MicOff className="w-3.5 h-3.5" /> : <Mic className="w-3.5 h-3.5" />}
              {isListening ? "Stop" : "Voice"}
            </button>

            {/* Prompts button */}
            <button 
              onClick={() => {
                clearAttachedFile();
                setInputText("Looking for a Senior Spring Boot Engineer with RabbitMQ backpressure configuration experience and PostgreSQL vector search query knowledge.");
              }}
              className="p-2 rounded-lg bg-white/5 hover:bg-white/10 hover:text-white text-gray-400 transition-colors duration-200 flex items-center gap-1.5 text-xs font-medium font-inter"
            >
              <Sparkles className="w-3.5 h-3.5" />
              Fill Template
            </button>

          </div>

          {/* Character counter */}
          <div className="font-mono text-[10px] text-gray-600 tracking-wider">
            {inputText.length} / 2000
          </div>
        </div>

      </div>

      {/* Credit Row */}
      <div className="flex items-center gap-4 mt-8 font-inter text-xs text-gray-600 select-none">
        <span>RabbitMQ Queue Ingest</span>
        <span className="w-1.5 h-1.5 rounded-full bg-white/10" />
        <span>Apache Tika OCR</span>
        <span className="w-1.5 h-1.5 rounded-full bg-white/10" />
        <span>OpenAI Embedding Vectorization</span>
        <span className="w-1.5 h-1.5 rounded-full bg-white/10" />
        <span>pgvector Storage</span>
      </div>

      {/* Semantic Search Results Panel */}
      {hasSearched && (
        <div className="w-full max-w-2xl mt-12 text-left flex flex-col gap-4 animate-fade-in">
          <div className="flex items-center justify-between border-b border-white/5 pb-2">
            <h3 className="font-fustat font-semibold text-sm tracking-wider text-purple-400 uppercase">Semantic search matches</h3>
            <span className="text-xs text-gray-500 font-inter font-medium">{searchResults.length} candidates found</span>
          </div>

          {searchResults.length === 0 ? (
            <div className="w-full p-8 rounded-2xl border border-dashed border-white/10 bg-white/[0.01] text-center font-inter text-sm text-gray-500">
              No matching profiles found in database. Please ingest some CVs first!
            </div>
          ) : (
            <div className="flex flex-col gap-4">
              {searchResults.map((candidate, idx) => (
                <div 
                  key={candidate.jobTrackingId || idx}
                  className="p-5 rounded-2xl border border-white/5 bg-white/[0.02] hover:bg-white/[0.04] hover:border-purple-500/25 transition-all duration-300 flex flex-col gap-3 group relative overflow-hidden"
                >
                  {/* Subtle hover gradient glow */}
                  <div className="absolute inset-0 bg-gradient-to-r from-purple-500/0 via-purple-500/[0.01] to-indigo-500/0 opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
                  
                  {/* Top Candidate Row */}
                  <div className="flex items-start justify-between">
                    <div className="flex flex-col">
                      <h4 className="font-fustat font-bold text-white text-base group-hover:text-purple-400 transition-colors">{candidate.fullName}</h4>
                      
                      {/* Contact metadata */}
                      <div className="flex flex-wrap items-center gap-x-4 gap-y-1 mt-1 font-inter text-xs text-gray-400">
                        <span className="flex items-center gap-1">
                          <Mail className="w-3.5 h-3.5 text-gray-500" />
                          {candidate.email}
                        </span>
                        <span className="flex items-center gap-1">
                          <Phone className="w-3.5 h-3.5 text-gray-500" />
                          {candidate.phoneNumber}
                        </span>
                      </div>
                    </div>

                    {/* Compatibility Match Score Pill */}
                    <div className="flex flex-col items-end">
                      <div className={`px-2.5 py-1 rounded-full font-mono text-xs font-bold ${
                        candidate.compatibilityScore >= 80 ? "bg-emerald-500/10 border border-emerald-500/20 text-emerald-400" :
                        candidate.compatibilityScore >= 60 ? "bg-purple-500/10 border border-purple-500/20 text-purple-400" :
                        "bg-gray-500/10 border border-gray-500/20 text-gray-400"
                      }`}>
                        {candidate.compatibilityScore.toFixed(1)}% Match
                      </div>
                      <span className="text-[10px] text-gray-600 font-medium uppercase tracking-wider mt-1.5">pgvector distance</span>
                    </div>
                  </div>

                  {/* Summary / Analysis */}
                  <p className="font-inter text-xs leading-relaxed text-gray-400">{candidate.summary}</p>

                  {/* Education info */}
                  {candidate.education && (
                    <div className="flex items-center gap-1.5 font-inter text-xs text-gray-500">
                      <GraduationCap className="w-3.5 h-3.5 text-purple-400/50" />
                      <span>{candidate.education}</span>
                      <span className="w-1 h-1 rounded-full bg-white/10" />
                      <span>{candidate.experienceYears} Years Exp</span>
                    </div>
                  )}

                  {/* Skills tags */}
                  {candidate.skills && (
                    <div className="flex flex-wrap gap-1.5 mt-1">
                      {candidate.skills.split(',').map((skill, sIdx) => (
                        <span 
                          key={sIdx} 
                          className="px-2 py-0.5 rounded bg-white/5 border border-white/5 text-[10px] font-inter text-gray-400 tracking-wide font-medium"
                        >
                          {skill.trim()}
                        </span>
                      ))}
                    </div>
                  )}

                </div>
              ))}
            </div>
          )}
        </div>
      )}

    </div>
  );
};

export default HeroContent;
