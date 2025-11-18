package com.example.studymate2.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.studymate2.data.StudyDatabase
import com.example.studymate2.data.StudyTask
import com.example.studymate2.data.TaskType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TaskSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: run {
            Log.w("TaskSyncWorker", "User not authenticated; deferring sync")
            return@withContext Result.success()
        }

        val dao = StudyDatabase.getDatabase(applicationContext).studyTaskDao()
        val repositoryTasks = dao.getTasksOnce()
        val remoteCollection = firestore.collection("users").document(user.uid).collection("tasks")

        try {
            repositoryTasks.forEach { task ->
                val docRef = task.externalId?.let { remoteCollection.document(it) }
                    ?: remoteCollection.document()
                val payload = mapOf(
                    KEY_TITLE to task.title,
                    KEY_SUBJECT to task.subject,
                    KEY_DUE_DATE to task.dueDate,
                    KEY_DURATION to task.durationMinutes,
                    KEY_TYPE to task.taskType.name,
                    KEY_COMPLETED to task.completed
                )
                docRef.set(payload).await()

                if (task.externalId == null) {
                    dao.updateTask(task.copy(externalId = docRef.id))
                }
            }

            val snapshot = remoteCollection.get().await()
            val existingByExternalId = repositoryTasks.associateBy { it.externalId }
            val merged = snapshot.documents.mapNotNull { doc ->
                val title = doc.getString(KEY_TITLE) ?: return@mapNotNull null
                val subject = doc.getString(KEY_SUBJECT) ?: return@mapNotNull null
                val dueDate = doc.getLong(KEY_DUE_DATE) ?: return@mapNotNull null
                val duration = doc.getLong(KEY_DURATION)?.toInt() ?: return@mapNotNull null
                val completed = doc.getBoolean(KEY_COMPLETED) ?: false
                val typeName = doc.getString(KEY_TYPE) ?: TaskType.ASSIGNMENT.name
                val taskType = runCatching { TaskType.valueOf(typeName) }.getOrDefault(TaskType.ASSIGNMENT)

                val localId = existingByExternalId[doc.id]?.id ?: 0
                StudyTask(
                    id = localId,
                    externalId = doc.id,
                    title = title,
                    subject = subject,
                    dueDate = dueDate,
                    durationMinutes = duration,
                    taskType = taskType,
                    completed = completed
                )
            }

            if (merged.isNotEmpty()) {
                dao.insertTasks(merged)
            }

            Result.success()
        } catch (ex: Exception) {
            if (ex is FirebaseFirestoreException && ex.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                Log.e("TaskSyncWorker", "Sync failed due to permissions; not retrying", ex)
                Result.failure()
            } else {
                Log.e("TaskSyncWorker", "Sync failed", ex)
                Result.retry()
            }
        }
    }

    companion object {
        private const val KEY_TITLE = "title"
        private const val KEY_SUBJECT = "subject"
        private const val KEY_DUE_DATE = "dueDate"
        private const val KEY_DURATION = "durationMinutes"
        private const val KEY_TYPE = "taskType"
        private const val KEY_COMPLETED = "completed"
    }
}