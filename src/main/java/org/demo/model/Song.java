package org.demo.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;

@SuppressWarnings("serial")
@Entity
@Table(name="SONGS")
@Indexed
public class Song implements Serializable{
    private long id;
    private String title;
    private String artist;
    private String album;
    private String notes;
    
    @Column(name="ALBUM", length=256)
    @Field(index=Index.YES)
    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }
    
    @Column(name="ARTIST", length=256)
    @Field(index=Index.YES)
    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
    @Id @GeneratedValue
    @Column(name="ID")
    @DocumentId
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    @Column(name="NOTES", length=256)
    @Field(index=Index.YES)
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    @Column(name="TITLE", length=255)
    @Field(index=Index.YES)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        StringBuilder stb = new StringBuilder();
        stb.append("[Title: ").append(title)
                .append(", Artist: ").append(artist)
                .append(", Album: ").append(album)
                .append(", Notes: ").append(notes).append("]");
        return stb.toString();
    }
}
