package com.example.notdefterim;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VeritabaniYardimcisi extends SQLiteOpenHelper {

    public VeritabaniYardimcisi(Context context) {
        super(context, "Notlarim.db", null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TARIH sütunu eklendi
        db.execSQL("CREATE TABLE notlar (ID INTEGER PRIMARY KEY AUTOINCREMENT, NOT_ICERIK TEXT, TARIH TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS notlar");
        onCreate(db);
    }

    public boolean notEkle(String icerik) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("NOT_ICERIK", icerik);

        // Şu anki tarih ve saati alıyoruz
        String suankiTarih = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        cv.put("TARIH", suankiTarih);

        long sonuc = db.insert("notlar", null, cv);
        return sonuc != -1;
    }

    // GÜNCELLEME FONKSİYONU
    public void notGuncelle(String eskiIcerik, String yeniIcerik) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("NOT_ICERIK", yeniIcerik);
        db.update("notlar", cv, "NOT_ICERIK = ?", new String[]{eskiIcerik});
    }

    public void notSil(String icerik) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("notlar", "NOT_ICERIK = ?", new String[]{icerik});
    }

    public Cursor tumVerileriGetir() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM notlar ORDER BY ID DESC", null); // En yeni not en üstte
    }

    public Cursor notAra(String kelime) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM notlar WHERE NOT_ICERIK LIKE ?", new String[]{"%" + kelime + "%"});
    }
}