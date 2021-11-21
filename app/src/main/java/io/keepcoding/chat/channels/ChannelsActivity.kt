package io.keepcoding.chat.channels

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Layout
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.keepcoding.chat.Channel
import io.keepcoding.chat.R
import io.keepcoding.chat.Repository
import io.keepcoding.chat.conversation.ConversationActivity
import io.keepcoding.chat.databinding.ActivityChannelsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class ChannelsActivity : AppCompatActivity() {

	val binding: ActivityChannelsBinding by lazy { ActivityChannelsBinding.inflate(layoutInflater) }
	val channelsAdapter: ChannelsAdapter by lazy { ChannelsAdapter(::openChannel) }
	val vm: ChannelsViewModel by viewModels {
        ChannelsViewModel.ChannelsViewModelProviderFactory(Repository)
	}


	override fun onCreate(savedInstanceState: Bundle?) {
		println("ORC: onCreate")
		super.onCreate(savedInstanceState)
		setContentView(binding.root)
		binding.topics.apply {
			adapter = channelsAdapter // -> A nuestro reciclerView le asignamos el adapter
			addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL)) // Añadimos una linea a cada fila
		}

		// Refresh View
		val swipeToRefresh = binding.swipeToRefresh
		swipeToRefresh.setOnRefreshListener {
			println("ORC: Se ha actualizado...")
			vm.loadChannels()
			swipeToRefresh.isRefreshing = false
		}

		vm.state.observe(this) {
			when (it) {
				is ChannelsViewModel.State.ChannelsReceived -> {
					println("ORC: ChannelsReceived")
					showLoading()
					channelsAdapter.submitList(it.channels)
					hideLoading()

				}
				is ChannelsViewModel.State.Error.ErrorLoading -> {
					println("ORC: ErrorLoading")
					Toast.makeText(this, "Error de servidor!", Toast.LENGTH_SHORT).show()
					reloadChannelsMessage()
					hideLoading()

				}
				is ChannelsViewModel.State.Error.ErrorWithChannels -> {
					println("ORC: ErrorWithChannels")
					Toast.makeText(this, "Error de sincronización!", Toast.LENGTH_SHORT).show()
					channelsAdapter.submitList(it.channels)
					hideLoading()

				}
				is ChannelsViewModel.State.LoadingChannels.Loading -> {
					println("ORC: LoadingChannels")
					showLoading()

				}
				is ChannelsViewModel.State.LoadingChannels.LoadingWithChannels -> {
					println("ORC: LoadingWithChannels")
					channelsAdapter.submitList(it.channels)

				}
			}
		}

	}

	private fun showLoading() {
		// TODO: Show loading
		binding.topics.isVisible = false
		binding.viewLoading.root.isVisible = true
		println("ORC: Ha pasado por showLoading")
	}

	private fun hideLoading() {
		// TODO: Hide loading
		binding.root.postDelayed({
			binding.topics.isVisible = true
			binding.viewLoading.root.isVisible = false
		},3000)
		println("ORC: Ha pasado por hideLoading")
	}

	override fun onResume() {
		super.onResume()
		vm.loadChannels()

	}

	private fun openChannel(channel: Channel) {
		startActivity(ConversationActivity.createIntent(this, channel))
	}

	private fun reloadChannelsMessage() {

		MaterialAlertDialogBuilder(this)
			.setTitle("Ojo!")
			.setMessage("No se han cargado los canales, por favor, vuelve a ejecutar la app o dar al boton Si")
			.setPositiveButton("Si") { dialog, which ->
				showLoading()
				vm.loadChannels()
			}
			.show()
	}

}