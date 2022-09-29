package tasklist

import kotlinx.datetime.*
import kotlin.system.exitProcess

class Task {
    private val taskContent = mutableListOf<String>()
    private lateinit var priority: String
    private var dateTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0"))

    private val duetag: String
        get() {
            val tmp = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date.daysUntil(dateTime.date)
            return when {
                tmp == 0 -> "T"
                tmp > 0 -> "I"
                else -> "O"
            }
        }

    private fun setPriority() {
        println("Input the task priority (C, H, N, L):")
        val input = readln().lowercase()
        if (input != "c" &&
            input != "h" &&
            input != "n" &&
            input != "l"
        )
            setPriority()

        priority = input
    }

    private fun setDate() {
        println("Input the date (yyyy-mm-dd):")
        val input = readln().lowercase()
        try {
            val (year, month, day) = input.split("-").map { it.toInt() }
            dateTime = LocalDateTime(year, month, day, 0, 0)
        } catch (e: Exception) {
            println("The input date is invalid")
            setDate()
        }
    }

    private fun setTime() {
        println("Input the time (hh:mm):")
        val input = readln().lowercase()
        try {
            val (hour, minute) = input.split(":").map { it.toInt() }
            dateTime = LocalDateTime(dateTime.year, dateTime.monthNumber, dateTime.dayOfMonth, hour, minute)

        } catch (e: Exception) {
            println("The input time is invalid")
            setTime()
        }
    }

    fun setNonContentData() {
        setPriority()
        setDate()
        setTime()
    }

    private fun addTaskContent(newContent: String) = taskContent.add(newContent)

    private fun getPriorityColor(): String = when (priority.lowercase()) {
        "c" -> "\u001B[101m \u001B[0m"
        "h" -> "\u001B[103m \u001B[0m"
        "n" -> "\u001B[102m \u001B[0m"
        "l" -> "\u001B[104m \u001B[0m"
        else -> ""
    }

    private fun getDueTagColor(): String = when (duetag.lowercase()) {
        "i" -> "\u001B[102m \u001B[0m"
        "t" -> "\u001B[103m \u001B[0m"
        "o" -> "\u001B[101m \u001B[0m"
        else -> ""
    }

    fun setContentData() {
        val input = readln()
        if (input.isBlank()) {
            if (this.noContent())
                println("The task is blank")
        } else {
            this.addTaskContent(input)
            setContentData()
        }
    }

    private fun formattedLine(s: String): MutableList<String> {
        var oldLines = s
        val formattedLines = mutableListOf<String>()

        while (oldLines.isNotEmpty()) {
            var line = ""
            for (i in 1..44) {
                if (oldLines.isNotEmpty()) {
                    line += oldLines.first()
                    oldLines = oldLines.drop(1)
                } else {
                    line += " "
                }
            }
            formattedLines.add(line)
        }

        return formattedLines
    }

    private fun addZero(number: Int): String {
        return if (number < 10) "0${number}" else number.toString()
    }

    private fun addSpaces(year: Int): String {
        var n = 1000
        var result = ""
        while (year % n == year) {

            result += " "
            n /= 10
        }
        return result + year.toString()
    }

    private fun getDate(): String {
        return "${addSpaces(dateTime.year)}-${addZero(dateTime.monthNumber)}-${addZero(dateTime.dayOfMonth)}"
    }

    private fun getTime(): String {
        return "${addZero(dateTime.hour)}:${addZero(dateTime.minute)}"
    }

    fun printTask(index: Int) {
        val newTaskContent = mutableListOf<String>()

        for (el in taskContent) {
            newTaskContent.addAll(formattedLine(el))
        }

        println("| ${index + 1}  | ${getDate()} | ${getTime()} | ${getPriorityColor()} | ${getDueTagColor()} |${newTaskContent[0]}|")

        for (i in 0 until newTaskContent.size - 1)
            println("|    |            |       |   |   |${newTaskContent[i + 1]}|")

        println("+----+------------+-------+---+---+--------------------------------------------+")

    }

    fun editTask() {
        println("Input a field to edit (priority, date, time, task):")
        when (readln()) {
            "priority" -> {
                setPriority()
                println("The task is changed")
            }

            "date" -> {
                setDate()
                println("The task is changed")
            }

            "time" -> {
                setTime()
                println("The task is changed")
            }

            "task" -> {
                taskContent.clear()
                println("Input a new task (enter a blank line to end):")
                setContentData()
                println("The task is changed")
            }

            else -> {
                println("Invalid field")
                editTask()
            }
        }
    }

    fun noContent() = taskContent.isEmpty()
}

class TaskList {
    private val taskList = mutableListOf<Task>()

    fun addTask() {
        val task = Task()
        task.setNonContentData()
        println("Input a new task (enter a blank line to end):")
        task.setContentData()
        if (!task.noContent())
            taskList.add(task)
    }

    fun printTasks() {
        println(
            "+----+------------+-------+---+---+--------------------------------------------+\n" +
                    "| N  |    Date    | Time  | P | D |                   Task                     |\n" +
                    "+----+------------+-------+---+---+--------------------------------------------+"
        )

        for (i in taskList.indices)
            taskList[i].printTask(i)
    }

    fun deleteTask() {
        println("Input the task number (1-${taskList.size}):")
        val input = readln().lowercase()
        try {
            taskList.removeAt(input.toInt() - 1)
            println("The task is deleted")
        } catch (e: Exception) {
            println("Invalid task number")
            deleteTask()
        }
    }

    fun editTask() {
        println("Input the task number (1-${taskList.size}):")
        val input = readln().lowercase()
        try {
            taskList[input.toInt() - 1].editTask()
        } catch (e: Exception) {
            println("Invalid task number")
            editTask()
        }
    }

    fun isEmpty() = taskList.isEmpty()
}

object Process {
    private val taskList = TaskList()

    fun start() {
        println("Input an action (add, print, edit, delete, end):")
        when (readln()) {
            "add" -> addTasks()
            "print" -> printTasks()
            "end" -> endProcess()
            "delete" -> deleteTask()
            "edit" -> editTask()
            else -> {
                println("The input action is invalid")
            }
        }
    }

    private fun deleteTask() {
        if (taskList.isEmpty()) {
            println("No tasks have been input")
        } else {
            printTasks()
            taskList.deleteTask()
        }
    }

    private fun editTask() {
        if (taskList.isEmpty()) {
            println("No tasks have been input")
        } else {
            printTasks()
            taskList.editTask()
        }
    }

    private fun addTasks() {
        taskList.addTask()
    }

    private fun printTasks() {
        if (taskList.isEmpty())
            println("No tasks have been input")
        else
            taskList.printTasks()
    }

    private fun endProcess() {
        println("Tasklist exiting!")
        exitProcess(1)
    }
}

fun main() {
    while (true) {
        Process.start()
    }
}
