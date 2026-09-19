package cl.baldomeronapoli.base.presentation.coordinator

import cl.baldomeronapoli.base.presentation.model.MessageType
import cl.baldomeronapoli.base.presentation.model.UiText
import cl.baldomeronapoli.base.presentation.model.UserMessage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserMessageCoordinatorTest {
    private val coordinator = UserMessageCoordinator()

    private fun createMessage(
        title: String = "Test",
        message: String = "Test message",
        type: MessageType = MessageType.DANGER,
        isSnackBar: Boolean = false,
        showDismissIcon: Boolean = false,
    ) = UserMessage(
        title = UiText.from(title),
        message = UiText.from(message),
        type = type,
        isSnackBar = isSnackBar,
        showDismissIcon = showDismissIcon,
    )

    @Test
    fun showMessageEmitsToSubscriber() =
        runTest {
            var emitted: UserMessage? = null
            val job = launch { emitted = coordinator.userMessage.first() }
            yield()

            coordinator.showMessage(createMessage(title = "Error", message = "Connection failed"))
            job.join()

            assertEquals(MessageType.DANGER, emitted?.type)
        }

    @Test
    fun showMessageEmitsMultipleMessagesInOrder() =
        runTest {
            val received = mutableListOf<MessageType>()
            val job = launch { coordinator.userMessage.collect { received += it.type } }
            yield()

            coordinator.showMessage(createMessage(type = MessageType.DANGER, message = "Error"))
            yield()
            coordinator.showMessage(createMessage(type = MessageType.SUCCESS, message = "OK"))
            yield()
            job.cancel()

            assertEquals(listOf(MessageType.DANGER, MessageType.SUCCESS), received)
        }

    @Test
    fun showMessagePreservesSnackBarFlag() =
        runTest {
            var emitted: UserMessage? = null
            val job = launch { emitted = coordinator.userMessage.first() }
            yield()

            coordinator.showMessage(createMessage(isSnackBar = true))
            job.join()

            assertTrue(emitted?.isSnackBar == true)
        }

    @Test
    fun showMessagePreservesDismissIconTrue() =
        runTest {
            var emitted: UserMessage? = null
            val job = launch { emitted = coordinator.userMessage.first() }
            yield()

            coordinator.showMessage(createMessage(showDismissIcon = true))
            job.join()

            assertTrue(emitted?.showDismissIcon == true)
        }

    @Test
    fun showMessagePreservesDismissIconFalse() =
        runTest {
            var emitted: UserMessage? = null
            val job = launch { emitted = coordinator.userMessage.first() }
            yield()

            coordinator.showMessage(createMessage(showDismissIcon = false))
            job.join()

            assertFalse(emitted?.showDismissIcon == true)
        }
}
