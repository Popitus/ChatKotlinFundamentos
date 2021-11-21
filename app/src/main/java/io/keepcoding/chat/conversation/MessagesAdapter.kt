package io.keepcoding.chat.conversation

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.keepcoding.chat.Message
import io.keepcoding.chat.Repository
import io.keepcoding.chat.User
import io.keepcoding.chat.databinding.EmptyViewBinding
import io.keepcoding.chat.databinding.ViewMessageBinding
import io.keepcoding.chat.databinding.ViewMessageSendBinding
import io.keepcoding.chat.extensions.inflater
import java.text.SimpleDateFormat
import java.util.*

class MessagesAdapter(
	diffUtilCallback: DiffUtil.ItemCallback<Message> = DIFF
) : ListAdapter<Message, RecyclerView.ViewHolder>(diffUtilCallback) {

	companion object {

		private const val SEND_MESSAGE = 0
		private const val RECEIVE_MESSAGE = 1

		val DIFF = object : DiffUtil.ItemCallback<Message>() {
			override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean =
				oldItem.id == newItem.id

			override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean =
				oldItem == newItem
		}
	}

	override fun getItemViewType(position: Int): Int {
		val message = currentList[position]
		if (message.sender.id == Repository.currentSender.id) {
			return  SEND_MESSAGE
		}else{
			return RECEIVE_MESSAGE
		}

	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return when (viewType) {
			SEND_MESSAGE -> MessageViewHolder(parent)
			else -> MessageReceiveViewHolder(parent)

		}
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = run {
		when (holder) {
			is MessageReceiveViewHolder -> holder.bind(getItem(position))
			is MessageViewHolder -> holder.bind((getItem(position)))
		}
	}

	// Class MessageViewHolder
	class MessageViewHolder(
		parent: ViewGroup,
		private val binding: ViewMessageBinding = ViewMessageBinding.inflate(
			parent.inflater,
			parent,
			false
		)
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(message: Message) {
			binding.channelName.text = "${message.sender.name}: ${message.text}"
			binding.imageView.setImageResource(message.sender.profileImageRes)
			binding.timeMessage.text = convertLongToTime(message.timestamp)

		}

	}

	// Class MessageReceiveViewHolder
	class MessageReceiveViewHolder(
		parent: ViewGroup,
		private val binding: ViewMessageSendBinding = ViewMessageSendBinding.inflate(
			parent.inflater,
			parent,
			false
		)
	) : RecyclerView.ViewHolder(binding.root) {

		fun bind(message: Message) {
			binding.channelNameSent.text = "${message.sender.name}: ${message.text}"
			binding.imageViewSent.setImageResource(message.sender.profileImageRes)
			binding.timeMessage.text = convertLongToTime(message.timestamp)


		}

	}

}

private fun convertLongToTime(time:Long): String {
	val date = Date(time)
	val format = SimpleDateFormat("HH:mm")
	return format.format(date)
}
