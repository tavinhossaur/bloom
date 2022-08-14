package com.example.bloom

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bloom.databinding.ActivityMainBinding
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var musicaAdapter : MusicaAdapter // Variável que leva a classe MusicAdapter
    private lateinit var setMenuDrawer : ActionBarDrawerToggle // Toggle do Drawer Layout

    // Declaração de objetos/classes estáticas para poder utilizar
    companion object{
        lateinit var listaMusicaMain : ArrayList<Musica> // Lista de músicas da tela principal
        lateinit var listaMusicaPesquisa : ArrayList<Musica> // Lista de músicas que aparecerá na pesquisa
        @SuppressLint("StaticFieldLeak")
        lateinit var binding : ActivityMainBinding // binding é a variável do ViewBinding para ligar as views ao código
        var pesquisando : Boolean = false // Variável para indentificar se o usuário está fazendo uma pesquisa de músicas
    }

    // Método chamado quando o aplicativo é iniciado
    override fun onCreate(savedInstanceState: Bundle?) {
        modoEscuro()
        super.onCreate(savedInstanceState)

        installSplashScreen()

        // Define o estilo customizado da action bar
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.action_bar_layout)
        // Elevação 0 na actionBar
        supportActionBar?.elevation = 0F

        // Inicialização do binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        // root ou getRoot retorna a view mais externa no arquivo de layout associado ao binding
        // no caso, a ActivityMainBinding (activity_main.xml)
        setContentView(binding.root)

        // Checagem de permissões quando o usuário entrar na tela principal
        if(checarPermissoes()){
            iniciarLayout()

            // Para retornar os dados do usuário das músicas favoritas
            FavoritosActivity.listaFavoritos = ArrayList()
            val editor= getSharedPreferences("Favoritos", MODE_PRIVATE)
            val jsonStringFavoritos = editor.getString("Lista de favoritos", null)
            val typeTokenFavoritos = object : TypeToken<ArrayList<Musica>>(){}.type

            if (jsonStringFavoritos != null){
                val dataFavoritos : ArrayList<Musica> = GsonBuilder().create().fromJson(jsonStringFavoritos, typeTokenFavoritos)
                FavoritosActivity.listaFavoritos.addAll(dataFavoritos)
            }

            // Para retornar os dados do usuário das playlists criadas
            PlaylistsActivity.playlists = ModeloPlaylist()
            val jsonStringPlaylists = editor.getString("Lista de playlists", null)

            if (jsonStringPlaylists != null){
                val dataPlaylists : ModeloPlaylist = GsonBuilder().create().fromJson(jsonStringPlaylists, ModeloPlaylist::class.java)
                PlaylistsActivity.playlists = dataPlaylists
            }
        }

        // Abrir a tela de playlists
        binding.playlistBtn.setOnClickListener {
            startActivity(Intent(this, PlaylistsActivity::class.java))
        }

        // Abrir a tela de favoritos
        binding.favoritosBtn.setOnClickListener {
            startActivity(Intent(this, FavoritosActivity::class.java))
        }

        // Randomizar as músicas
        binding.randomBtn.setOnClickListener {
            // A lista de músicas precisa conter pelo menos uma música para funcionar
            if (listaMusicaMain.size < 1){
                // Criação de um toast para informar o usuário de que ele não possui músicas suficientes para utilizar a funcionalidade
                Toast.makeText(this, "Você não possui músicas!", Toast.LENGTH_SHORT).show()
            }else{
            // Se houver uma ou mais músicas, leve o usuário para o player com os dados para randomizá-la
            val mainIntent = Intent(this@MainActivity, PlayerActivity::class.java)
            // Quando o usuário é levado a tela do player, também é enviado os dados de posição da música (Int)
            mainIntent.putExtra("indicador", 0)
            // Quando o usuário é levado a tela do player, também é enviado os dados da classe da MainActivity (String)
            mainIntent.putExtra("classe", "Main")
            startActivity(mainIntent)
            }
        }

        // Abrir a gaveta lateral de opções (Drawer)
        setMenuDrawer = ActionBarDrawerToggle(this, binding.root, R.string.drawer_aberto, R.string.drawer_fechado)
        // Adiciona um listener para o layout do Drawer
        binding.root.addDrawerListener(setMenuDrawer)
        // Sincroniza o ícone do drawer ao estado dele
        setMenuDrawer.syncState()
        // Mostra o ícone de menu
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Muda a cor do ícone do drawer
        setMenuDrawer.drawerArrowDrawable.color = ContextCompat.getColor(this, R.color.white)

        /*  binding.navLayout.setNavigationItemSelectedListener {
              when(it.itemId){
                  R.id.item_musicas ->
                  R.id.item_albuns ->
                  R.id.item_artistas ->
                  R.id.item_favoritos ->
                  R.id.item_playlists ->
                  R.id.item_config ->
                  R.id.item_equalizador ->
                  R.id.item_sobre ->
              }
              true
          }
          */
    }

    // Método para deixar o aplicativo no seu modo padrão
    private fun modoEscuro(){
        application.setTheme(R.style.Theme_Bloom)
        setTheme(R.style.Theme_Bloom)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black3)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black3)
    }

    // Método que faz a checa se foram concedidas as permissões que o app precisa
    private fun checarPermissoes() : Boolean{
        // Se o aplicativo ainda não tiver a permissão concedida, fara a requisição da mesma, caso contrário, nada acontece e a pessoa pode utilizar o aplicativo normalmente
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(applicationContext, PermissaoActivity::class.java))
                finish() // Impede que o usuário volte a essa tela usando o botão voltar do celular
            }, 500)
            return false
        }
        return true
    }

    // Método para chamar tudo que será a inicialização do layout da tela inicial
    private fun iniciarLayout(){
        pesquisando = false
        // Lista de músicas
        listaMusicaMain = procurarMusicas()
        if(listaMusicaMain.size < 1){
            binding.avisoMusicas.visibility = View.VISIBLE
            binding.musicasRv.visibility = View.GONE
        }else{
            binding.avisoMusicas.visibility = View.GONE
            binding.musicasRv.visibility = View.VISIBLE
        }
        // Para otimização do RecyclerView, o seu tamanho é fixo,
        // mesmo quando itens são adicionados ou removidos.
        binding.musicasRv.setHasFixedSize(true)
        // Para otimização do RecyclerView, 13 itens fora da tela serão "segurados"
        // para depois potencialmente usá-los de novo (Reciclagem de itens).
        binding.musicasRv.setItemViewCacheSize(13)
        // O LayoutManager é responsável por medir e posicionar as visualizações dos itens dentro de um RecyclerView,
        // bem como determinar a política de quando reciclar visualizações de itens que não são mais visíveis para o usuário.
        binding.musicasRv.layoutManager = LinearLayoutManager(this@MainActivity)

        // Criando uma variável do Adapter com o contexto (tela) e a lista de músicas que será adicionada
        // ao RecyclerView por meio do mesmo Adapter
        musicaAdapter = MusicaAdapter(this@MainActivity, listaMusicaMain)
        // Setando o Adapter para este RecyclerView
        binding.musicasRv.adapter = musicaAdapter
        // Evita que o usuário consiga clicar em dois itens ao mesmo tempo
        binding.musicasRv.isMotionEventSplittingEnabled = false
    }

    // Método que faz a procura de músicas pelos arquivos do celular
    @SuppressLint("Recycle", "Range")
    private fun procurarMusicas(): ArrayList<Musica>{
        // Lista temporária de músicas
        val tempLista = ArrayList<Musica>()
        // Condições para a seleção das músicas dos arquivos "!= 0" (diferente de 0) significa que o cursor procurará apenas músicas
        // e não ringtones do android " AND " título não igual a "AUD%" ou seja, o título da música não pode ser igual a
        // AUD + zero ou outros caracteres.
        val selectMusica = MediaStore.Audio.Media.IS_MUSIC + "!= 0" + " AND " + MediaStore.Audio.Media.TITLE + " NOT  LIKE  'AUD%'"

        // Array de dados que serão retornados dos arquivos
        val dadosMusica = arrayOf(
            MediaStore.Audio.Media._ID, // ID da música
            MediaStore.Audio.Media.TITLE, // Título da música
            MediaStore.Audio.Media.ARTIST, // Artista da música
            MediaStore.Audio.Media.ALBUM, // Álbum da música
            MediaStore.Audio.Media.DURATION, // Duração da música
            MediaStore.Audio.Media.ALBUM_ID, // ID do álbum (imagem)
            // MediaStore.Audio.Media.DATE_ADDED, // Data de quando a música foi adicionada ao aplicativo
            MediaStore.Audio.Media.DATA) // Caminho da música

        // Cursor é o mecanismo que faz a busca e seleciona as músicas e as organiza com base nas condições passadas nos parâmetros,
        // para a organização alfabética, está sendo utilizado o código em SQL "COLLATE NOCASE ASC", sendo COLLATE a cláusula que
        // define uma ordenação, essa ordenação recebe como argumentos o "NOCASE" que torna a ordenação case-insensitive e "ASC" seria
        // "ascendente" ou seja, alfabéticamente de A a Z.
        val cursor = this.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            dadosMusica,
            selectMusica,
            null,
            MediaStore.Audio.Media.DISPLAY_NAME + " COLLATE NOCASE ASC",
            null)

        // If que, se houverem arquivos de áudios, o cursor começará a passar por todos eles um por um,
        // retornando seus dados e os adicionando ao modelo do array da música (Musica.kt),
        // e então as adicionando para a lista de músicas temporarias
        if (cursor != null) {
            if (cursor.moveToFirst())
                do {
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)) // Cursor procura e adiciona o ID da música
                    val tituloC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)) // Cursor procura e adiciona o título da música
                    val artistaC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)) // Cursor procura e adiciona o artista da música
                    val albumC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)) // Cursor procura e adiciona o álbuns da música
                    val duracaoC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)) // Cursor procura e adiciona o duração da música
                    val albumIdC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toString() // Cursor procura e adiciona o id do álbum da música
                    val uri = Uri.parse("content://media/external/audio/albumart") // Link onde ficará as imagens dos álbuns que o cursor deve retornar
                    val imagemUriC = Uri.withAppendedPath(uri, albumIdC).toString() // A imagemUri é a junção do id com o link da imagem
                    val caminhoC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)) // Cursor procura e adiciona o caminho da música

                    // Passando os dados retornados da música para o modelo do array da música (Musica.kt)
                    val musica = Musica(id = idC, titulo = tituloC, artista = artistaC, album = albumC, duracao = duracaoC, caminho = caminhoC, imagemUri = imagemUriC)
                    // Passando o caminho da música para uma constante que o identifica como um arquivo
                    val arquivo = File(musica.caminho)
                    // Se o arquivo existir, ele será adicionado para a lista de músicas,
                    // se ele foi excluído, não aparecerá mais na lista quando o aplicativo for iniciado novamente
                    if (arquivo.exists())
                        tempLista.add(musica)

                } while (cursor.moveToNext())
            cursor.close() // Termina o cursor, para que ele não fique executando o loop de pesquisa infinitamente
        }
        return tempLista // Retorna a lista de músicas para o ArrayList<Musica>
    }

    // Método para seleção de itens do menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (setMenuDrawer.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

    // Método para mostrar (inflar) o menu na action bar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.pesquisa_menu, menu)
        val pesquisaView = menu.findItem(R.id.pesquisa_view)?.actionView as SearchView
        // Texto da hint
        pesquisaView.queryHint = "Procure por título, artista, álbum..."
        // Muda a cor da hint da textView da pesquisa
        pesquisaView.findViewById<TextView>(androidx.appcompat.R.id.search_src_text).setHintTextColor(ContextCompat.getColor(this, R.color.grey3))

        // Fica lendo o que o usuário está digitando na barra de pesquisa
        pesquisaView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            // O texto é sempre enviado, com ou sem a confirmação de pesquisa do usuário
            // Dessa forma, a lista atualiza na hora com base na pesquisa do usuário
            override fun onQueryTextSubmit(query: String?): Boolean = true

            // Quando o texto muda
            override fun onQueryTextChange(textoPesquisa: String?): Boolean {
                // Cria a lista de músicas da pesquisa
                listaMusicaPesquisa = ArrayList()
                // Se o texto da pesquisa não for nulo
                if (textoPesquisa != null){
                    // Passa o texto dela para caixa baixa para pode encontrar a música de forma mais fácil
                    val pesquisa = textoPesquisa.lowercase()
                    // Para cada música na lista de músicas da tela principal
                    for (musica in listaMusicaMain)
                        // Se o título, artista ou álbum da música, em caixa baixa conter o texto da pesquisa
                        if (musica.titulo.lowercase().contains(pesquisa) || musica.artista.lowercase().contains(pesquisa) || musica.album.lowercase().contains(pesquisa)){
                            // Então adicione essa música a lista de pesquisa
                            listaMusicaPesquisa.add(musica)
                            // Defina pesquisando como true
                            pesquisando = true
                            // E atualize a lista de músicas pesquisadas
                            musicaAdapter.atualizarLista(listaPesquisa = listaMusicaPesquisa)
                        }
                }
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    // Método onDestroy, para encerrar o processo do aplicativo
    override fun onDestroy() {
        super.onDestroy()
        // Se a música não estiver tocando e a reprodução de músicas for nula
        if (!PlayerActivity.tocando && PlayerActivity.musicaService != null){
            // Então chame o método para encerrar o processo inteiro do app.
            encerrarProcesso()
        }
    }

    // Método onResume, para quando o usuário volta a activity
    override fun onResume() {
        super.onResume()
        // SharedPreferences, para salvar a lista de favoritos do usuário
        val editor = getSharedPreferences("Favoritos", MODE_PRIVATE).edit()
        val jsonStringFavoritos = GsonBuilder().create().toJson(FavoritosActivity.listaFavoritos)
        editor.putString("Lista de favoritos", jsonStringFavoritos)
        // SharedPreferences, para salvar a lista de playlists do usuário
        val jsonStringPlaylists = GsonBuilder().create().toJson(PlaylistsActivity.playlists)
        editor.putString("Lista de playlists", jsonStringPlaylists)
        editor.apply()

        // Quando retornado a tela, e o serviço de música não for nulo, então torne o Miniplayer visível.
        if(PlayerActivity.musicaService != null){
            binding.miniPlayer.visibility = View.VISIBLE
        }
    }
}