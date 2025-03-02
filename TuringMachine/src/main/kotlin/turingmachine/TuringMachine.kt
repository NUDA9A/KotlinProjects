package turingmachine

import kotlin.math.max

class TuringMachine(
    private val startingState: String,
    private val acceptedState: String,
    private val rejectedState: String,
    transitions: Collection<TransitionFunction>,
) {
    private val transitionMap: Map<Pair<String, Char>, Transition> = transitions.associateBy {
        it.state to it.symbol
    }.mapValues { it.value.transition }

    fun initialSnapshot(input: String): Snapshot {
        return Snapshot(startingState, Tape(input))
    }

    fun simulateStep(snapshot: Snapshot): Snapshot {
        val currChar = snapshot.tape.content[snapshot.tape.position]
        val newTape = snapshot.tape
        val transition = transitionMap[snapshot.state to currChar]

        return transition?.let {
            Snapshot(
                it.newState,
                newTape.applyTransition(it.newSymbol, it.move),
            )
        } ?: Snapshot(rejectedState, newTape)
    }

    fun simulate(initialString: String): Sequence<Snapshot> {
        return generateSequence(initialSnapshot(initialString)) {
            if (it.state != acceptedState && it.state != rejectedState) {
                simulateStep(it)
            } else {
                null
            }
        }
    }

    class Snapshot(val state: String, val tape: Tape) {
        fun applyTransition(transition: Transition): Snapshot {
            tape.applyTransition(transition.newSymbol, transition.move)
            return Snapshot(transition.newState, tape)
        }
        override fun equals(other: Any?): Boolean {
            if (other !is Snapshot) {
                return false
            }

            return this.state == other.state && this.tape == other.tape
        }

        override fun hashCode(): Int {
            var result = state.hashCode()
            result = 31 * result + tape.hashCode()
            return result
        }

        override fun toString(): String {
            return tape.toString()
        }

        fun copy(): Snapshot {
            return Snapshot(state, tape.copy())
        }
    }

    class Tape(initialString: String) {
        private constructor(initialString: String, position: Int) : this(initialString) {
            head += position
            this.position = position
        }

        var position: Int = 0
        private var head: Int = initialString.length
        private var rightPointer = -1
        private var needUpdate = false
        private var tapeSize: Int = 3 * initialString.length
        private var nonBlank: Int = initialString.length
        private var tape: CharArray = CharArray(3 * initialString.length) { i ->
            if (i in initialString.length..<2 * initialString.length) {
                initialString[i - initialString.length]
            } else {
                BLANK
            }
        }
        val content: CharArray
            get() {
                return if (tape.isEmpty()) {
                    CharArray(1) { _ -> BLANK }
                } else {
                    if (needUpdate || rightPointer == -1) {
                        rightPointer = tape.indexOfLast { it != BLANK }.coerceAtMost(tape.size - 1)
                        needUpdate = false
                    }

                    val left = head - position
                    val right = max(rightPointer, head)
                    tape.slice(left..right).toCharArray()
                }
            }

        fun applyTransition(char: Char, move: TapeTransition): Tape {
            if (tape.isEmpty()) {
                return applyToEmptyTape(char, move)
            }

            if (char == BLANK && tape[head] != BLANK && nonBlank == 1) {
                tape = CharArray(0)
                nonBlank = 0
                position = 0
                tapeSize = 15
                head = 5
                return this
            }

            if (char != BLANK && tape[head] == BLANK) {
                if (rightPointer < head) {
                    rightPointer = head
                }
                nonBlank++
            }
            if (char == BLANK && tape[head] != BLANK) {
                if (head == rightPointer) {
                    needUpdate = true
                }
                nonBlank--
            }

            tape[head] = char

            when (move) {
                TapeTransition.Stay -> {}
                TapeTransition.Right -> {
                    if (head + 1 == tape.size) {
                        expandToFit()
                        head = tapeSize / 3 * 2
                    } else {
                        head++
                    }

                    if ((position == 0 && char != BLANK) || position != 0) {
                        position++
                    }
                }
                TapeTransition.Left -> {
                    if (head == 0) {
                        expandToFit()
                        head = tapeSize / 3 - 1
                    } else {
                        head--
                    }

                    position = if (position == 0) 0 else position - 1
                }
            }

            return this
        }

        override fun toString(): String {
            if (tape.isEmpty()) {
                return ""
            }

            return buildString {
                for (c: Char in content) {
                    append(c)
                }
            }
        }

        fun copy(): Tape {
            return Tape(this.toString(), position)
        }

        override fun equals(other: Any?): Boolean {
            if (other !is Tape) {
                return false
            }

            return this.content.contentEquals(other.content) && this.position == other.position
        }

        private fun expandToFit() {
            val newTape = CharArray(3 * tapeSize) { i ->
                if (i in tapeSize..<2 * tapeSize) {
                    tape[i - tapeSize]
                } else {
                    BLANK
                }
            }
            tapeSize *= 3
            tape = newTape
        }

        private fun applyToEmptyTape(char: Char, move: TapeTransition): Tape {
            if (char == BLANK) {
                return this
            }

            tapeSize = 15
            head = 5
            tape = CharArray(tapeSize) { _ -> BLANK }
            nonBlank++
            when (move) {
                TapeTransition.Right -> {
                    tape[5] = char
                    position++
                    head++
                }
                TapeTransition.Stay -> {
                    tape[5] = char
                }
                TapeTransition.Left -> {
                    tape[6] = char
                }
            }

            return this
        }

        override fun hashCode(): Int {
            var result = position
            result = 31 * result + content.contentHashCode()
            return result
        }
    }
}
