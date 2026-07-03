package com.internship.cvworker.service;

import com.internship.cvworker.model.dto.ParsedCandidateDto;

import java.util.UUID;

public interface LlmParserService {
    ParsedCandidateDto parseCv(String resumeText, UUID jdId);
}
