package turingmachine

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.double
import java.io.BufferedReader
import java.io.File

class TuringMachineSimulator : CliktCommand() {
    private val machineFile by argument(help = "File containing Turing machine description")
    private val inputFile by argument(help = "File containing input word").optional()
    private val auto by option("--auto", help = "Run in automatic mode").flag(default = false)
    private val delay by option("--delay", help = "Delay between steps in seconds").double().default(0.5)

    private var startingState = ""
    private var acceptedState = ""
    private var rejectedState = ""
    private var blankSymbol = BLANK
    private val transitions = mutableListOf<TransitionFunction>()

    override fun run() {
        val machine = parseMachine(machineFile)

        val inputWord = if (inputFile != null) readInputFile(inputFile!!) else readInputFromConsole()

        simulateMachine(machine, inputWord, auto, delay)
    }

    private fun parseMachine(file: String): TuringMachine {
        val reader = BufferedReader(File(file).reader())

        reader.use {
            var line: String? = reader.readLine()
            while (line != null) {
                val trimmedLine = line.trim()

                when {
                    trimmedLine.startsWith("start:") -> {
                        startingState = trimmedLine.removePrefix("start:").trim()
                    }

                    trimmedLine.startsWith("accept:") -> {
                        acceptedState = trimmedLine.removePrefix("accept:").trim()
                    }

                    trimmedLine.startsWith("reject:") -> {
                        rejectedState = trimmedLine.removePrefix("reject:").trim()
                    }

                    trimmedLine.startsWith("blank:") -> {
                        blankSymbol = trimmedLine.removePrefix("blank:").trim().first()
                    }

                    else -> {
                        val parts = trimmedLine.split(" ")

                        if (parts.size == 6 && parts[2] == "->") {
                            val state = parts[0]
                            val symbol = if (parts[1].first() == blankSymbol) BLANK else parts[1].first()
                            val newState = parts[3]
                            val newSymbol = if (parts[4].first() == blankSymbol) BLANK else parts[4].first()
                            val move = when (parts[5]) {
                                "<" -> TapeTransition.Left
                                ">" -> TapeTransition.Right
                                "^" -> TapeTransition.Stay
                                else -> throw IllegalArgumentException("Unknown move direction: ${parts[5]}")
                            }

                            transitions.add(TransitionFunction(state, symbol, move, newSymbol, newState))
                        } else {
                            println(parts[2] + " " + parts.size)
                            throw IllegalArgumentException("Invalid line format: $trimmedLine")
                        }
                    }
                }
                line = reader.readLine()
            }
        }

        return TuringMachine(startingState, acceptedState, rejectedState, transitions)
    }

    private fun readInputFile(file: String): String {
        val fileObj = File(file)
        require(fileObj.exists()) { "Input file does not exist: $file" }
        require(fileObj.isFile) { "Input path is not a file: $file" }

        val reader = BufferedReader(fileObj.reader())
        return reader.use { it.readLine() ?: "" }
    }

    private fun readInputFromConsole(): String {
        print("Введите входное слово: ")
        return readlnOrNull() ?: ""
    }

    private fun simulateMachine(
        machine: TuringMachine,
        inputWord: String,
        autoMode: Boolean,
        delayInSeconds: Double,
    ) {
        val simulation = machine.simulate(inputWord)

        simulation.forEach { snapshot ->
            println("Current state: ${snapshot.state}")
            println("${snapshot.tape}")
            for (i in 0..<snapshot.tape.position) {
                print(" ")
            }
            println("^")

            if (snapshot.state == acceptedState) {
                println("The machine has reached the accepting state!")
                return@forEach
            } else if (snapshot.state == rejectedState) {
                println("The machine has reached the rejecting state.")
                return@forEach
            }

            if (autoMode) {
                Thread.sleep((delayInSeconds * 1000).toLong())
            } else {
                println("Press Enter to proceed to the next step...")
                System.console()?.readLine() ?: readlnOrNull()
            }
        }
    }
}

fun main(args: Array<String>): Unit = TuringMachineSimulator().main(args)
