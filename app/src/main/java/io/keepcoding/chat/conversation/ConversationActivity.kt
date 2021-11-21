package io.keepcoding.chat.conversation

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.keepcoding.chat.Channel
import io.keepcoding.chat.Message
import io.keepcoding.chat.Repository
import io.keepcoding.chat.common.TextChangedWatcher
import io.keepcoding.chat.databinding.ActivityConversationBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ConversationActivity : AppCompatActivity() {
	private var logica: Boolean = true

	private val binding: ActivityConversationBinding by lazy {
		ActivityConversationBinding.inflate(layoutInflater)
	}
	private val vm: ConversationViewModel by viewModels {
		ConversationViewModel.ConversationViewModelProviderFactory(Repository)
	}
	private val messagesAdapter: MessagesAdapter = MessagesAdapter()
	private val channelId: String by lazy { intent.getStringExtra(CHANNEL_ID)!! }


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(binding.root)
		binding.conversation.apply {
			layoutManager = LinearLayoutManager(context).apply {
				stackFromEnd = true
			}
			adapter = messagesAdapter
		}
		vm.state.observe(this) {
			when (it) {
				is ConversationViewModel.State.MessagesReceived -> {
					println("ORC - Conversation: MessageReceived")
					binding.emptyTextView.isVisible = false
					renderMessages(it.messages)
					hideLoading()
				}
				is ConversationViewModel.State.Error.ErrorLoading -> {
					println("ORC - Conversation: ErrorLoading")
					println("ORC - ${it.throwable.message}")
					reloadConversationMessage()
					showLoading()
					hideLoading()
				}
				is ConversationViewModel.State.Error.ErrorWithMessages -> {
					println("ORC - Conversation: ErrorWithMessages")
					Toast.makeText(this, "Error al enviar mensaje, enviar de nuevo", Toast.LENGTH_LONG).show()
					hideLoading()
				}
				is ConversationViewModel.State.LoadingMessages.Loading -> {
					println("ORC - Conversation: LoadingMessages.Loading")

				}
				is ConversationViewModel.State.LoadingMessages.LoadingWithMessages -> {
					println("ORC - Conversation: LoadingMessages.LoadingWithMessages")
					renderMessages(it.messages)
					showLoading()
					hideLoading()

				}
			}
		}
		vm.message.observe(this) {
			binding.tvMessage.apply {
				setText(it)
				setSelection(it.length)
			}
		}
		vm.sendButtonEnabled.observe(this) {
			if (it) {
				binding.sendButton.alpha = 1.0F
			} else {
				binding.sendButton.alpha = 0.5F
			}
			binding.sendButton.isEnabled = it

		}

		binding.tvMessage.addTextChangedListener(TextChangedWatcher(vm::onInputMessageUpdated))

		binding.sendButton.setOnClickListener {
				vm.sendMessage(channelId)
		}
	}

	private fun renderMessages(messages: List<Message>) {
		messagesAdapter.submitList(messages) {
			println("ORC -> submitList Messages ${messages.size}")
			if (messages.isNotEmpty()) {
				binding.conversation.smoothScrollToPosition(messages.size)
			} else {
				binding.emptyTextView.isVisible = true
			}
		}
	}

	private fun showLoading() {
		// TODO: Show loading
		binding.conversation.isVisible = false
		binding.viewLoading.root.isVisible = true
		println("ORC: Ha pasado por showLoading")
	}

	private fun hideLoading() {
		// TODO: Hide loading
		if (logica) {
			binding.root.postDelayed({
				binding.conversation.isVisible = true
				binding.viewLoading.root.isVisible = false
			},3000)
		}
		logica = false

		println("ORC: Ha pasado por hideLoading")
	}

	override fun onResume() {
		super.onResume()
		vm.loadConversation(channelId)

	}

	companion object {
		const val CHANNEL_ID = "CHANNEL_ID"

		fun createIntent(context: Context, channel: Channel): Intent =
			Intent(
				context,
				ConversationActivity::class.java
			).apply {
				putExtra(CHANNEL_ID, channel.id)
			}
	}

	private fun reloadConversationMessage() {

		MaterialAlertDialogBuilder(this)
			.setTitle("Oh no!")
			.setMessage("Hay un problema en la red, por favor pulsar si para recargar")
			.setPositiveButton("Si") { dialog, which ->
				showLoading()
				vm.loadConversation(channelId)
			}
			.show()
	}
}