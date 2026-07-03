package com.internship.cvingestion.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RabbitMQMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID trackingId;
    private String fileStoragePath;
    private String fileName;
    private String fileType;
    private UUID jdId;
    private LocalDateTime timestamp;
}
