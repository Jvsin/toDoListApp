package com.example.todolistapp

data class TaskItem(
    val title: String,
    val description: String,
//    val creationTime: Long,
//    val executionTime: Long,
//    val status: TaskStatus,
    val notificationEnabled: Boolean,
//    val category: String,
//    val attachments: List<Attachment>
)

enum class TaskStatus {
    COMPLETED,
    INCOMPLETE
}

data class Attachment(
    val type: AttachmentType,
    val url: String
)

enum class AttachmentType {
    IMAGE,
    FILE
}