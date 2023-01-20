package com.example.demomymusicplayer;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.*;
import java.net.URL;
import java.util.*;



public class HelloController implements Initializable
{
    private MediaPlayer mediaPlayer;
    private List<Song> songs = new ArrayList<>();
    private List<Playlist> playlists;
    private int currentSongIndex = 0;
    private int currentIndex = 0;
    private boolean isPlaying = false;

    @FXML
    private MediaView mediaView;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private ListView<String> songsList;
    @FXML
    private ListView<String> playlistsListView;
    @FXML
    private ListView<String> createdPlaylistsListView;
    @FXML
    private Label songTitleLabel;
    @FXML
    private Label songArtistLabel;
    @FXML
    private Label remainingTimeLabel;
    @FXML
    private TextField searchField;
    @FXML
    private ImageView myImageView;
    private ImageViewer imageViewer;



    //En class til at navngive en sang med Navn, Artist og angive et Media Link
    class Song
    {
        private String name;
        private String artist;
        private Media media;
        public Song(String name, String artist, Media media)
        {
            this.name = name;
            this.artist = artist;
            this.media = media;
        }
        public String getName() {
            return name;
        }
        public String getArtist() {
            return artist;
        }
        public Media getMedia() {
            return media;
        }
    }
    public HelloController(){
        playlists = new ArrayList<>();
    }



    //Funktion til se billeder
    public class ImageViewer {
        private ArrayList<Image> images = new ArrayList<>();
        private Random random = new Random();
        private ImageView imageView;
        public ImageViewer(ImageView imageView) {
            this.imageView = imageView;
        }
        public void loadImages(String folder) {
            File dir = new File(folder);
            for (File file : dir.listFiles()) {
                images.add(new Image(file.toURI().toString()));
            }
        }



        //Funktion til se Random billeder
        public void startSlideshow() {
            Timeline timeline = new Timeline();
            timeline.setCycleCount(Animation.INDEFINITE);
            KeyFrame keyFrame = new KeyFrame(Duration.seconds(30), event -> {
                imageView.setImage(getRandomImage());
            });
            timeline.getKeyFrames().add(keyFrame);
            timeline.play();
        }
        private Image getRandomImage() {
            return images.get(random.nextInt(images.size()));
        }
    }



    //Funktion til at tilføje en folder med flere billeder
    @FXML
    private void openFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(new Stage());
        if(selectedDirectory != null){
            imageViewer.loadImages(selectedDirectory.getAbsolutePath());
            imageViewer.startSlideshow();
        }
    }



    //Funktion til at tilføjet en playliste
    @FXML
    private void createPlaylist() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Playlist");
        dialog.setHeaderText("Enter a name for the new playlist:");
        dialog.setContentText("Name:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            String name = result.get();
            Playlist newPlaylist = new Playlist(name);
            playlists.add(newPlaylist);
            updatePlaylistsListView();

            DB.insertSQL("INSERT INTO playlists (name, date_created) VALUES (?, NOW());");
        }
    }



    //Funktion til at tilføjet varighed på en playliste
    private void updatePlaylistsListView() {
        ObservableList<String> playlistsNames = FXCollections.observableArrayList();
        for (Playlist playlist : playlists) {
            playlistsNames.add(playlist.getName() + " - " + playlist.getTotalDuration());
        }
        playlistsListView.setItems(playlistsNames);
        createdPlaylistsListView.setItems(playlistsNames);


    }



    //Funktion ti at tilføje en sang til en playliste
    @FXML
    private void addToPlaylist() {
        HelloController.Song selectedSong = songs.get(songsList.getSelectionModel().getSelectedIndex());
        Playlist selectedPlaylist = playlists.get(playlistsListView.getSelectionModel().getSelectedIndex());
        selectedPlaylist.addSong(selectedSong);
        updatePlaylistsListView();
        updatePlaylistSongsListView(selectedPlaylist);

        DB.insertSQL("INSERT INTO playlist_songs (playlist_id, song_id) VALUES (?,?)");
    }



    //Funktion til opdatere en playliste
    private void updatePlaylistSongsListView(Playlist playlist) {
        ObservableList<String> songsNames = FXCollections.observableArrayList();
        for (HelloController.Song song : playlist.getSongs()) {
            songsNames.add(song.getName());
        }
        createdPlaylistsListView.setItems(songsNames);
    }



    //Funktion til at slette en playliste
    @FXML
    private void deletePlaylist()
    {
        int selectedIndex = playlistsListView.getSelectionModel().getSelectedIndex();
        playlists.remove(selectedIndex);
        updatePlaylistsListView();
        updatePlaylistSongsListView(null);

        DB.deleteSQL("DELETE FROM playlists WHERE playlist_id = ?;");
        DB.deleteSQL("DELETE FROM playlist_songs WHERE playlist_id = ?;");
    }



    //Funktion til afspille den næste sange i en playliste
    private void playNext() {
        if(isPlaying && currentIndex<songs.size()-1) {
            currentIndex++;
            mediaPlayer = new MediaPlayer(songs.get(currentIndex).getMedia());
            songTitleLabel.setText(songs.get(currentIndex).getName());
            songArtistLabel.setText(songs.get(currentIndex).getArtist());
            mediaPlayer.play();
        }
    }



    //Funktion til afspille en playliste
    private void playPlaylist(Playlist playlist) {
        if(!isPlaying){
            isPlaying = true;
            List<HelloController.Song> songs = playlist.getSongs();
            if(songs.size()>0) {
                currentIndex = 0;
                mediaPlayer.stop();
                mediaPlayer = new MediaPlayer(songs.get(currentIndex).getMedia());
                songTitleLabel.setText(songs.get(currentIndex).getName());
                songArtistLabel.setText(songs.get(currentIndex).getArtist());
                mediaPlayer.play();
                mediaPlayer.setOnEndOfMedia(() -> {
                    playNext();
                });
            }
        }
    }
    @FXML
    private void playPlaylist(ActionEvent event) {
        Playlist selectedPlaylist = playlists.get(playlistsListView.getSelectionModel().getSelectedIndex());
        playPlaylist(selectedPlaylist);
    }



    //Funktion til at slette en sang fra en playliste
    @FXML
    private void deleteFromPlaylist() {
        Playlist selectedPlaylist = playlists.get(playlistsListView.getSelectionModel().getSelectedIndex());
        String selectedSongName = createdPlaylistsListView.getSelectionModel().getSelectedItem();
        for (HelloController.Song song : selectedPlaylist.getSongs()) {
            if (song.getName().equals(selectedSongName)) {
                selectedPlaylist.removeSong(song);
                break;
            }
        }
        updatePlaylistSongsListView(selectedPlaylist);
    }



    //Funktion til at lave en søgefunktion
    @FXML
    private void handleSearchButtonAction(ActionEvent event) {
        String query = searchField.getText();
        List<Song> searchResults = new ArrayList<>();
        for (Song song : songs) {
            if (song.getName().contains(query) || song.getArtist().contains(query)) {
                searchResults.add(song);
            }
        }
        songsList.getItems().clear();

        ObservableList<String> searchResultsNames = FXCollections.observableArrayList();
        for (Song song : searchResults) {
            searchResultsNames.add(song.getName());
        }
        songsList.setItems(searchResultsNames);
    }



    //Den grafiske overflade til progress bar som farvelægger processen.
    private void updateProgress()
    {
        progressBar.setProgress(mediaPlayer.getCurrentTime().toMillis() / mediaPlayer.getTotalDuration().toMillis());
    }




    public void initialize(URL location, ResourceBundle resources) {
        updatePlaylistsListView();
        imageViewer = new ImageViewer(myImageView);
        imageViewer.loadImages("C:\\Users\\tfm\\IdeaProjects\\REALMusicPlayer\\src\\main\\java\\com\\example\\demomymusicplayer\\ImageFolder");
        imageViewer.startSlideshow();



        //Alle Sang links og deres Navn og Artist
        songs.add(new Song("Stranger Things", "AlexiAction", new Media(new File("src/main/java/com/example/demomymusicplayer/media/stranger-things-124008.mp3").toURI().toString())));
        songs.add(new Song("Sunset Rider", "Music Unlimited", new Media(new File("src/main/java/com/example/demomymusicplayer/media/sunset-rider-112776.mp3").toURI().toString())));
        songs.add(new Song("Synthwave 80s", "Grand Project", new Media(new File("src/main/java/com/example/demomymusicplayer/media/synthwave-80s-110045.mp3").toURI().toString())));
        songs.add(new Song("Rock N Christmas", "Musictown", new Media(new File("src/main/java/com/example/demomymusicplayer/media/rock-n-christmas-80s-127420.mp3").toURI().toString())));
        songs.add(new Song("Neon Lights ", "EvgnyBardyuha", new Media(new File("src/main/java/com/example/demomymusicplayer/media/neon-lights-128797.mp3").toURI().toString())));
        songs.add(new Song("Lady of The 80s", "AleXZavesa", new Media(new File("src/main/java/com/example/demomymusicplayer/media/lady-of-the-80x27s-128379.mp3").toURI().toString())));
        songs.add(new Song("Insurrection", "REDproductions", new Media(new File("src/main/java/com/example/demomymusicplayer/media/insurrection-10941.mp3").toURI().toString())));
        songs.add(new Song("Fun Disco", "Grand Project", new Media(new File("src/main/java/com/example/demomymusicplayer/media/fun-disco-1-108497.mp3").toURI().toString())));
        songs.add(new Song("Dreamy Inspiring Electronic", "FASSounds", new Media(new File("src/main/java/com/example/demomymusicplayer/media/dreamy-inspiring-electronic-15015.mp3").toURI().toString())));
        songs.add(new Song("Digital Love", "AlexiAction", new Media(new File("src/main/java/com/example/demomymusicplayer/media/digital-love-127441.mp3").toURI().toString())));
        songs.add(new Song("Cyberpunk 2099 ", "DopeStuff", new Media(new File("src/main/java/com/example/demomymusicplayer/media/cyberpunk-2099-10701.mp3").toURI().toString())));
        songs.add(new Song("Hero Of The 80s ", "Slicebeats", new Media(new File("src/main/java/com/example/demomymusicplayer/media/a-hero-of-the-80s-126684.mp3").toURI().toString())));
        songs.add(new Song("Pop Futuristic Electronic", "Lightyeartraxx", new Media(new File("src/main/java/com/example/demomymusicplayer/media/80s-synthwave-analog-synth-pop-futuristic-electronic-music-20598.mp3").toURI().toString())));
        songs.add(new Song("Synthwave Retro Synthpop ", "Eidunn", new Media(new File("src/main/java/com/example/demomymusicplayer/media/80s-retrowave-synthwave-retro-synthpop-futuristic-electro-pop-music-20596.mp3").toURI().toString())));
        songs.add(new Song("Synthwave Pop Retro Synth ", "Playsound", new Media(new File("src/main/java/com/example/demomymusicplayer/media/80s-futuristic-analog-synthwave-pop-retro-synth-music-20595.mp3").toURI().toString())));



        //Funktion til at tilføje alle sangene til vores listViewer
        for (Song song : songs)
        {
            songsList.getItems().add(song.getName());
        }
        mediaPlayer = new MediaPlayer(songs.get(currentSongIndex).getMedia());
        mediaView.setMediaPlayer(mediaPlayer);
        mediaPlayer.setAutoPlay(true);



        //Funktion til at se resterende tid af en sang
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            double remainingTime = mediaPlayer.getTotalDuration().toMillis() - mediaPlayer.getCurrentTime().toMillis();
            remainingTimeLabel.setText(String.format("%02d:%02d", (int) (remainingTime / 60000), (int) (remainingTime % 60000 / 1000)));
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();



        //Funktion til at spole frem til tilbage i Progressbar
        progressBar.setOnMouseClicked(event ->
        {
            double mouseX = event.getX();
            double progressBarWidth = progressBar.getWidth();
            double progress = mouseX / progressBarWidth;
            mediaPlayer.seek(mediaPlayer.getTotalDuration().multiply(progress));
        });
    }



    //Funktion til at vælge en sang fra vores songList og efterfølgende afspillere sangen
    @FXML
    private void songSelected(MouseEvent event)
    {
        String song = songsList.getSelectionModel().getSelectedItem();
        for (int i = 0; i < songs.size(); i++)
        {
            if (songs.get(i).getName().equals(song))
            {
                currentSongIndex = i;
                break;
            }
        }
        mediaPlayer.stop();
        mediaPlayer = new MediaPlayer(songs.get(currentSongIndex).getMedia());
        mediaView.setMediaPlayer(mediaPlayer);
        mediaPlayer.play();
        mediaPlayer.currentTimeProperty().addListener((observable, oldTime, newTime) -> updateProgress());
        songTitleLabel.setText(songs.get(currentSongIndex).getName());
        songArtistLabel.setText(songs.get(currentSongIndex).getArtist());
    }



    //Handlers til at Pause, Stop og Play.
    @FXML private void handlePause()
    {
        mediaPlayer.pause();
    }
    @FXML private void handleStop()
    {
        mediaPlayer.stop();
    }
    @FXML private void handlePlay()
    {
        mediaPlayer.play();
    }
}

