package com.example.notdefterim;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    VeritabaniYardimcisi myDb;
    EditText editNot, editAra;
    FloatingActionButton fabKaydet;
    ListView listeNotlar;
    LinearLayout layoutBosListe;
    ArrayList<String> notListesi;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Nesneleri Bağlama
        myDb = new VeritabaniYardimcisi(this);
        editNot = findViewById(R.id.editTextNot);
        editAra = findViewById(R.id.editTextAra);
        fabKaydet = findViewById(R.id.fabEkle);
        listeNotlar = findViewById(R.id.listViewNotlar);
        layoutBosListe = findViewById(R.id.layoutBosListe);

        notListesi = new ArrayList<>();


        listeyiGuncelle("");


        fabKaydet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String icerik = editNot.getText().toString().trim();
                if (!icerik.isEmpty()) {
                    boolean eklendiMi = myDb.notEkle(icerik);
                    if (eklendiMi) {
                        Toast.makeText(MainActivity.this, "Not başarıyla eklendi", Toast.LENGTH_SHORT).show();
                        editNot.setText("");
                        listeyiGuncelle("");


                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(editNot.getWindowToken(), 0);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Lütfen bir not yazın!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        listeNotlar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String eskiNot = notListesi.get(position);
                final EditText input = new EditText(MainActivity.this);
                input.setText(eskiNot);
                input.setPadding(50, 40, 50, 40);

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Notu Düzenle")
                        .setView(input)
                        .setPositiveButton("Güncelle", (dialog, which) -> {
                            String yeniNot = input.getText().toString().trim();
                            if (!yeniNot.isEmpty()) {
                                myDb.notGuncelle(eskiNot, yeniNot);
                                listeyiGuncelle("");
                                Toast.makeText(MainActivity.this, "Güncellendi", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("İptal", null)
                        .show();
            }
        });



        // 3. ARAMA İŞLEMİ
        editAra.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                listeyiGuncelle(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // 4. LİSTEYİ GÜNCELLEYEN FONKSİYON
    public void listeyiGuncelle(String arananKelime) {
        notListesi.clear();
        Cursor veriler;

        if (arananKelime.isEmpty()) {
            veriler = myDb.tumVerileriGetir();
        } else {
            veriler = myDb.notAra(arananKelime);
        }

        if (veriler == null || veriler.getCount() == 0) {
            layoutBosListe.setVisibility(View.VISIBLE);
            listeNotlar.setVisibility(View.GONE);
        } else {
            layoutBosListe.setVisibility(View.GONE);
            listeNotlar.setVisibility(View.VISIBLE);
            while (veriler.moveToNext()) {
                notListesi.add(veriler.getString(1));
            }
        }

        // ÖZEL ADAPTER YAPISI
        adapter = new ArrayAdapter<String>(this, R.layout.not_satiri, R.id.textViewNotIcerik, notListesi) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                String currentNot = notListesi.get(position);

                // Kelime Sayısı
                TextView txtKelime = view.findViewById(R.id.textViewKelimeSayisi);
                int count = currentNot.trim().isEmpty() ? 0 : currentNot.trim().split("\\s+").length;
                txtKelime.setText(count + " kelime");

                // Paylaş Butonu
                ImageView imgPaylas = view.findViewById(R.id.imageViewPaylas);
                imgPaylas.setOnClickListener(v -> {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, currentNot);
                    sendIntent.setType("text/plain");
                    Intent shareIntent = Intent.createChooser(sendIntent, "Notu Paylaş");
                    startActivity(shareIntent);
                });

                // --- YENİ: SİLME (ÇÖP KOVASI) BUTONU ---
                ImageView imgSil = view.findViewById(R.id.imageViewSil);
                imgSil.setOnClickListener(v -> {
                    // Silme Onay Penceresi (AlertDialog)
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Notu Sil")
                            .setMessage("Bu notu silmek istediğinize emin misiniz?")
                            .setIcon(android.R.drawable.ic_delete)
                            .setPositiveButton("Evet, Sil", (dialog, which) -> {
                                myDb.notSil(currentNot); // O anki notu sil
                                listeyiGuncelle(""); // Listeyi yenile
                                Toast.makeText(MainActivity.this, "Silindi", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Hayır", null)
                            .show();
                });

                return view;
            }
        };
        listeNotlar.setAdapter(adapter);
    }
}