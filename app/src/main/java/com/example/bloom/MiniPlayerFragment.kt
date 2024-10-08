package com.example.bloom

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.bloom.PlayerActivity.Companion.favoritado
import com.example.bloom.databinding.FragmentMiniPlayerBinding

// A classe do miniplayer é um Fragment, uma porção ou parte de uma interface de usuário
class MiniPlayerFragment : Fragment(){

    // Declaração de um objeto/classe estática
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentMiniPlayerBinding // binding é a variável do ViewBinding para ligar as views ao código
        var proxMusica : Boolean = false
    }

    // Criação da view do miniplayer
    @SuppressLint("UseCompatLoadingForColorStateLists")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val viewMiniPlayer = inflater.inflate(R.layout.fragment_mini_player, container, false)
        // Inicialização do binding
        binding = FragmentMiniPlayerBinding.bind(viewMiniPlayer)
        binding.root.visibility = View.INVISIBLE

        // Ajuste de cores para o modo escuro do Android
        if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_NO){
            binding.root.setBackgroundResource(R.drawable.miniplayer_style)
            binding.miniplayerView.setCardBackgroundColor(ContextCompat.getColorStateList(requireContext(), R.color.white))
        }else{
            binding.miniplayerView.setCardBackgroundColor(ContextCompat.getColorStateList(requireContext(), R.color.black3))
            binding.tituloMusicaMp.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey2))
            binding.btnFavMp.setColorFilter(ContextCompat.getColor(requireContext(), R.color.grey2), android.graphics.PorterDuff.Mode.SRC_IN)
            binding.btnProxMp.setColorFilter(ContextCompat.getColor(requireContext(), R.color.grey2), android.graphics.PorterDuff.Mode.SRC_IN)
        }

        // Quando clicado no miniplayer o usuário é levado para o player
        binding.miniplayerView.setOnClickListener {
            val miniPlayerIntent = Intent(requireContext(), PlayerActivity::class.java)
            // Quando o usuário é levado a tela do player, também é enviado os dados de posição da música (Int)
            miniPlayerIntent.putExtra("indicador", PlayerActivity.posMusica)
            // Quando o usuário é levado a tela do player, também é enviado os dados da classe do miniplayer (String)
            miniPlayerIntent.putExtra("classe", "MiniPlayer")
            ContextCompat.startActivity(requireContext(), miniPlayerIntent, null)
        }

        // FUNÇÕES DE CONTROLE DO MINIPLAYER
        // Ao clicar no botão de favorito, favorita a música atual do player
        binding.btnFavMp.setOnClickListener {
            PlayerActivity.favIndex = checarFavoritos(PlayerActivity.filaMusica[PlayerActivity.posMusica].id)
            // Muda a animação do botão ao ser clicado
            binding.btnFavMp.startAnimation(AnimationUtils.loadAnimation(context, androidx.appcompat.R.anim.abc_grow_fade_in_from_bottom))
            // Se a música já estiver favoritada
            if (favoritado){
                // Então defina a variável favoritado para false
                favoritado = false
                // Mude o ícone para o coração vazio de desfavoritado
                binding.btnFavMp.setImageResource(R.drawable.ic_round_favorite_border_miniplayer_24)
                setBtnsNotify()
                // E remova a música da lista de favoritos utilizando o indicador favIndex
                FavoritosActivity.listaFavoritos.removeAt(PlayerActivity.favIndex)
                // Caso contrário (música desfavoritada)
            }else{
                // Então defina a variável favoritado para true
                favoritado = true
                // Mude o ícone para o coração cheio de favoritado
                binding.btnFavMp.setImageResource(R.drawable.ic_round_favorite_miniplayer_24)
                setBtnsNotify()
                // E adicione a música atual a lista de favoritos
                FavoritosActivity.listaFavoritos.add(PlayerActivity.filaMusica[PlayerActivity.posMusica])
            }
        }

        // Ao clicar no botão "next", chama o método trocar música
        // com valor "true" para o Boolean "proximo"
        binding.btnProxMp.setOnClickListener {
            // Muda a animação do botão ao ser clicado
            binding.btnProxMp.startAnimation(AnimationUtils.loadAnimation(context, androidx.appcompat.R.anim.abc_grow_fade_in_from_bottom))
            mudarPosMusica(adicionar = true)
            PlayerActivity.musicaService!!.criarPlayer()
            carregarMusica()
            // Muda o ícone da barra de notificação
            setBtnsNotify()
            tocar()
            // Define que a música mudou
            proxMusica = true
            MainActivity.musicaAdapter.atualizarLista(MainActivity.listaMusicaMain)
        }
        return viewMiniPlayer
    }

    // Método para mostrar o miniplayer
    override fun onResume() {
        super.onResume()
        // Se o serviço da música não estiver nulo
        if (PlayerActivity.musicaService != null){
            // Mostre o miniplayer
            binding.root.visibility = View.VISIBLE
            // Muda o tamanho da fading edge do recyler view
            MainActivity.binding.musicasRv.setFadingEdgeLength(250)
            // Seleciona o título da música para título se mover
            binding.tituloMusicaMp.isSelected = true
            // e carregue os dados da música nele
            carregarMusica()
        }
    }

    // Método para carregar os dados da música
    private fun carregarMusica(){
        PlayerActivity.favIndex = checarFavoritos(PlayerActivity.filaMusica[PlayerActivity.posMusica].id)
        Glide.with(requireContext())
            // Carrega a posição da música e a uri da sua imagem
            .load(PlayerActivity.filaMusica[PlayerActivity.posMusica].imagemUri)
            // Faz a aplicação da imagem com um placeholder caso a música não tenha nenhuma imagem ou ela ainda não tenha sido carregada
            .apply(RequestOptions().placeholder(R.drawable.placeholder_grey).centerCrop())
            // Alvo da aplicação da imagem
            .into(binding.imgMusicaMp)

        // Carrega os dados corretos para a música atual sendo reproduzida
        binding.tituloMusicaMp.text = PlayerActivity.filaMusica[PlayerActivity.posMusica].titulo
        binding.artistaMusicaMp.text = PlayerActivity.filaMusica[PlayerActivity.posMusica].artista

        // Verica se a música carregada está favoritada
        if (favoritado) {
            binding.btnFavMp.setImageResource(R.drawable.ic_round_favorite_miniplayer_24)
        } else{
            binding.btnFavMp.setImageResource(R.drawable.ic_round_favorite_border_miniplayer_24)
        }
    }

    // Método para tocar a música pela barra de notificação
    private fun tocar(){
        // Toca a música e muda o ícone do botão na barra e no player
        PlayerActivity.tocando = true
        checarFavoritos(PlayerActivity.filaMusica[PlayerActivity.posMusica].id)
        PlayerActivity.musicaService!!.mPlayer!!.start()
        if (favoritado) {
            PlayerActivity.musicaService!!.mostrarNotificacao(R.drawable.ic_round_pause_notify_24, R.drawable.ic_round_favorite_24)
        } else{
            PlayerActivity.musicaService!!.mostrarNotificacao(R.drawable.ic_round_pause_notify_24, R.drawable.ic_round_favorite_border_24)
        }
        PlayerActivity.binding.btnPpTpl.setImageResource(R.drawable.ic_round_pause_circle_24)
    }
}