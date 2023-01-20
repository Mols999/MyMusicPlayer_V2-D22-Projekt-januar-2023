module com.example.demomymusicplayer {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires javafx.media;
    requires java.sql;
    requires sqljdbc4;

    opens com.example.demomymusicplayer to javafx.fxml;
    exports com.example.demomymusicplayer;
}