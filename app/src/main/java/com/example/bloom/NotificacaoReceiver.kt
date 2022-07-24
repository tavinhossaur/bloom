package com.example.bloom

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.ColorFilterTransformation

// A classe NotificacaoReceiver tem como principal função receber as intents de controle da música na barra de notificação
// e lidar com as mesmas
class NotificacaoReceiver : BroadcastReceiver() {
    // Método que, quando receber uma intent
    override fun onReceive(context: Context?, intent: Intent?) {
        // Analisará se ela é, por meio da declaração when, a intent de randomizar, voltar uma música, tocar, etc.
        // E executará a ação da intent específica.
        when(intent?.action){
            // Chama o método para randomizar as músicas
            /* ClasseApplication.REPETIR ->
                if(!PlayerActivity.repetindo){
                    reproducaoNormal()
                }else{
                    reproducaoRepetir()
                } */
            // Chama o método para voltar para música anterior
            ClasseApplication.ANTERIOR -> proxMusicaAnte(proximo = false, context = context!!)
            // Chama o método para tocar ou pausar a música atual
            ClasseApplication.TOCAR ->
                // Se estiver tocando, então pause e torne possível a exclusão da barra de notificação
                if(PlayerActivity.tocando){
                    pausar()
                    PlayerActivity.musicaService!!.stopForeground(false)
                // Caso contrário (Não estiver tocando), então toque a música
                }else{
                    tocar()
                }
            // Chama o método para ir para próxima música
            ClasseApplication.PROXIMO -> proxMusicaAnte(proximo = true, context = context!!)
            // Chama o método para favoritar a música
            ClasseApplication.FAVORITAR -> favoritar()
            // Quando a barra de notificação for limpa, chame o método para resetar o player
            ClasseApplication.LIMPAR -> pausarPlayer()
            }
        }
    }

    // Método para tocar a música pela barra de notificação
    private fun tocar(){
        // Toca a música e muda o ícone dos botões na barra e no player
        PlayerActivity.tocando = true
        PlayerActivity.musicaService!!.mPlayer!!.start()
        setBtnsNotify()
        PlayerActivity.binding.btnPpTpl.setImageResource(R.drawable.ic_round_pause_circle_24)
        MiniPlayerFragment.binding.btnPpMp.setImageResource(R.drawable.ic_round_pause_circle_24)
    }

    // Método para pausar a música pela barra de notificação
    private fun pausar(){
        // Pausa a música e muda o ícone do botões na barra e no player
        PlayerActivity.tocando = false
        PlayerActivity.musicaService!!.mPlayer!!.pause()
        setBtnsNotify()
        PlayerActivity.binding.btnPpTpl.setImageResource(R.drawable.ic_round_play_circle_24)
        MiniPlayerFragment.binding.btnPpMp.setImageResource(R.drawable.ic_round_play_circle_24)
    }

    private fun favoritar(){
        PlayerActivity.favIndex = checarFavoritos(PlayerActivity.filaMusica[PlayerActivity.posMusica].id)
        // Se a música já estiver favoritada
        if (PlayerActivity.favoritado){
            // Então defina a variável favoritado para false
            PlayerActivity.favoritado = false
            // Mude o ícone para o coração vazio de desfavoritado
            PlayerActivity.binding.btnFavTpl.setImageResource(R.drawable.ic_round_favorite_border_24)
            MiniPlayerFragment.binding.tituloMusicaMp.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_round_favorite_border_miniplayer_24,0)
            setBtnsNotify()
            // E remova a música da lista de favoritos utilizando o indicador favIndex
            FavoritosActivity.listaFavoritos.removeAt(PlayerActivity.favIndex)
            // Caso contrário (música desfavoritada)
        }else{
            // Então defina a variável favoritado para true
            PlayerActivity.favoritado = true
            // Mude o ícone para o coração cheio de favoritado
            PlayerActivity.binding.btnFavTpl.setImageResource(R.drawable.ic_round_favorite_24)
            MiniPlayerFragment.binding.tituloMusicaMp.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_round_favorite_miniplayer_24,0)
            setBtnsNotify()
            // E adicione a música atual a lista de favoritos
            FavoritosActivity.listaFavoritos.add(PlayerActivity.filaMusica[PlayerActivity.posMusica])
        }
    }

    // Método para checar se o usuário clicou no botão próxima música ou música anterior,
    // e sincronizar a música atual do player com a música atual que aparece na barra de notificação da música
    private fun proxMusicaAnte(proximo : Boolean, context: Context){
        if (!PlayerActivity.repetindo){
            mudarPosMusica(adicionar = proximo)
            PlayerActivity.musicaService!!.criarPlayer()
            PlayerActivity.musicaService!!.carregarMusica()
            Glide.with(context)
                // Carrega a posição da música e a uri da sua imagem
                .load(PlayerActivity.filaMusica[PlayerActivity.posMusica].imagemUri)
                // Faz a aplicação da imagem com um placeholder caso a música não tenha nenhuma imagem ou ela ainda não tenha sido carregada
                .apply(RequestOptions().placeholder(R.drawable.bloom_lotus_icon_grey).centerCrop())
                // Alvo da aplicação da imagem
                .into(PlayerActivity.binding.imgMusicaTpl)

            val multiTransform = MultiTransformation<Bitmap>(
                // Efeito embaçado na imagem
                BlurTransformation(40, 5),
                // Filtro de cor escuro na imagem
                ColorFilterTransformation(Color.argb(70, 40, 40, 40))
            )

            // MiniPlayer
            Glide.with(context)
                // Carrega a posição da música e a uri da sua imagem
                .load(PlayerActivity.filaMusica[PlayerActivity.posMusica].imagemUri)
                // Faz a aplicação da imagem com um placeholder caso a música não tenha nenhuma imagem ou ela ainda não tenha sido carregada
                .apply(RequestOptions().placeholder(R.drawable.bloom_lotus_icon_grey).centerCrop())
                // Alvo da aplicação da imagem
                .into(MiniPlayerFragment.binding.imgMusicaMp)

            // Carrega os dados corretos para a música atual sendo reproduzida
            MiniPlayerFragment.binding.tituloMusicaMp.text = PlayerActivity.filaMusica[PlayerActivity.posMusica].titulo
            MiniPlayerFragment.binding.artistaMusicaMp.text = PlayerActivity.filaMusica[PlayerActivity.posMusica].artista

            Glide.with(context)
                // Carrega a posição da música e a uri da sua imagem
                .load(PlayerActivity.filaMusica[PlayerActivity.posMusica].imagemUri)
                // Faz a aplicação da imagem com as transformações e um placeholder caso a música não tenha nenhuma imagem
                // ou ela ainda não tenha sido carregada
                .apply(RequestOptions.bitmapTransform(multiTransform))
                // Alvo da aplicação da imagem, como o alvo da aplicação não é um componente simples, e sim, o background de um LinearLayout
                // é necessário utilizar o método CustomTraget do Glide.
                .into(object : CustomTarget<Drawable?>() {
                    // Quando retornar a imagem
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable?>?) {
                        // Aplica a imagem da música ao background do root da activity, ou seja, o LinearLayout que engloba todas as views.
                        PlayerActivity.binding.root.background = resource
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        return
                    }
                })
            // Chama o método para tocar a música
            tocar()
            // Checa se a música atual está favoritada
            PlayerActivity.favIndex = checarFavoritos(PlayerActivity.filaMusica[PlayerActivity.posMusica].id)
            if (PlayerActivity.favoritado){
                PlayerActivity.binding.btnFavTpl.setImageResource(R.drawable.ic_round_favorite_24)
                MiniPlayerFragment.binding.tituloMusicaMp.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_round_favorite_miniplayer_24,0)
                PlayerActivity.musicaService!!.mostrarNotificacao(R.drawable.ic_round_pause_notify_24, R.drawable.ic_round_favorite_24)

            }else{
                PlayerActivity.binding.btnFavTpl.setImageResource(R.drawable.ic_round_favorite_border_24)
                MiniPlayerFragment.binding.tituloMusicaMp.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_round_favorite_border_miniplayer_24,0)
                PlayerActivity.musicaService!!.mostrarNotificacao(R.drawable.ic_round_pause_notify_24, R.drawable.ic_round_favorite_border_24)
            }
        }
    }

    // Método para pausar o player quando a barra de notificação da música for apagada.
    // assim, quando o usuário a apagar, e voltar a tela do player, ele poderá retomar a música novamente de onde ela parou.
    private fun pausarPlayer(){
        try {
            // Se a variável do player for nula, então ela se torna o MediaPlayer
            if (PlayerActivity.musicaService!!.mPlayer == null) PlayerActivity.musicaService!!.mPlayer = MediaPlayer()
            // .pause = Pausa o player com a música no tempo em que ela parou
            PlayerActivity.musicaService!!.mPlayer!!.pause()
        } catch (e: Exception) {
            return
        }
    }

   /* private fun reproducaoNormal(){
        PlayerActivity.repetindo = false
        PlayerActivity.binding.btnRepetir.setImageResource(R.drawable.ic_baseline_repeat_24)
        PlayerActivity.binding.btnRepetir.setColorFilter(R.color.black1)
    }

    private fun reproducaoRepetir(){
        PlayerActivity.repetindo = true
        PlayerActivity.binding.btnRepetir.setImageResource(R.drawable.ic_baseline_repeat_one_24)
        PlayerActivity.binding.btnRepetir.setColorFilter(R.color.purple1)
    } */