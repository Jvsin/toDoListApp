package com.example.todolistapp

data class TaskItem(
    val title: String,
    val description: String,
//    val creationTime: Long,
    val deadline: Long,
//    val status: TaskStatus,
    val notificationEnabled: Boolean,
//    val category: Categories,
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

enum class Categories {
    SCHOOL,
    WORK,
    HOME
}