package it.unive.raccoltapp.ui;


import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;

import it.unive.raccoltapp.R;

public class WasteGuideFragment extends Fragment {

    public WasteGuideFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Gonfia il layout per questo frammento
        View view = inflater.inflate(R.layout.fragment_waste_guide, container, false);

        // --- Configurazione Categorie ---

        // NOTA: Ho aggiunto il parametro 'view' all'inizio di ogni chiamata

        // --- 1. CARTA E CARTONE ---
        setupCategory(view, R.id.cat_paper,
                "Carta e Cartone",
                R.color.waste_paper,
                R.drawable.ic_cartahq,
                "• Giornali, riviste, fumetti\n• Scatole di cartone (imballaggi)\n• Sacchetti di carta puliti\n• Libri, quaderni, depliant\n• Cartone della pizza (se pulito)",
                "• Carta plastificata o oleata\n• Carta sporca di cibo (es. cartone pizza unto)\n• Fazzoletti usati (vanno nell'umido)\n• Scontrini fiscali (carta termica)",
                "💡 Suggerimento: Appiattisci sempre le scatole per ridurre il volume. Rimuovi nastro adesivo e punti metallici grandi."
        );

        // --- 2. PLASTICA E LATTINE ---
        setupCategory(view, R.id.cat_plastic,
                "Plastica e Lattine",
                R.color.waste_plastic,
                R.drawable.ic_plasticahq,
                "• Bottiglie d'acqua e bibite\n• Flaconi detersivi e shampoo\n• Vasetti yogurt e vaschette alimentari\n• Lattine in alluminio e scatolette tonno\n• Polistirolo (imballaggi piccoli)",
                "• Giocattoli di plastica\n• Posate di plastica dura\n• Bacinelle e arredi da giardino\n• Oggetti in gomma o silicone\n• Tubi per irrigazione",
                "💡 Suggerimento: Schiaccia le bottiglie per il lungo (non dall'alto) e richiudi il tappo. Sciacqua velocemente le vaschette."
        );

        // --- 3. VETRO ---
        setupCategory(view, R.id.cat_glass,
                "Vetro",
                R.color.waste_glass,
                R.drawable.ic_vetrohq,
                "• Bottiglie di vetro (vino, birra, olio)\n• Vasetti di marmellata e sottaceti\n• Boccette di profumo vuote\n• Barattoli in vetro per alimenti",
                "• Piatti e tazzine in ceramica o porcellana\n• Bicchieri di cristallo\n• Specchi e vetri finestre\n• Lampadine e neon\n• Pyrex (pirofile da forno)",
                "💡 Suggerimento: Non serve lavare a fondo, basta svuotare bene. Togli sempre il tappo (che va nella plastica o metallo)."
        );

        // --- 4. ORGANICO (UMIDO) ---
        setupCategory(view, R.id.cat_organic,
                "Organico (Umido)",
                R.color.waste_organic,
                R.drawable.ic_organicohq,
                "• Scarti di cucina (frutta, verdura, carne, pesce)\n• Fondi di caffè e bustine tè\n• Fiori recisi e piccole potature\n• Tovaglioli di carta sporchi di cibo\n• Tappi di sughero",
                "• Pannolini e assorbenti (se non compostabili)\n• Lettiere per animali sintetiche\n• Liquidi e oli frittura (vanno all'isola ecologica)\n• Mozziconi di sigaretta",
                "💡 Suggerimento: Utilizza esclusivamente sacchetti biodegradabili e compostabili. Non usare mai buste di plastica normali."
        );

        // --- 5. SECCO (INDIFFERENZIATO) ---
        setupCategory(view, R.id.cat_general,
                "Secco (Indifferenziato)",
                R.color.waste_general,
                R.drawable.ic_secco_indifferenziatahq,
                "• Carta oleata o plastificata\n• Pannolini e assorbenti\n• CD, DVD, videocassette\n• Giocattoli, penne, spazzolini\n• Lamette e rasoi usa e getta\n• Ceramica e porcellana (piccole quantità)",
                "• Tutti i rifiuti riciclabili (Carta, Plastica, Vetro, Umido)\n• Rifiuti Pericolosi (Farmaci, Pile, Vernici)\n• RAEE (Elettrodomestici, Cellulari)",
                "💡 Suggerimento: Il secco è l'ultima spiaggia! Se hai dubbi, consulta il dizionario dei rifiuti nell'app prima di gettare qui."
        );

        return view;
    }

    private void setupCategory(View parentView, int includeId, String title, int colorResId, int iconResId, String tYes, String tNo, String tTip) {
        View categoryView = parentView.findViewById(includeId);
        MaterialCardView cardView = (MaterialCardView) parentView.findViewById(includeId);

        // Trova i componenti
        TextView tvTitle = categoryView.findViewById(R.id.tv_category_name);
        FrameLayout iconContainer = categoryView.findViewById(R.id.icon_container);
        ImageView imgIcon = categoryView.findViewById(R.id.img_icon);
        ImageView imgArrow = categoryView.findViewById(R.id.img_arrow);

        View headerLayout = categoryView.findViewById(R.id.layout_header);
        View detailsLayout = categoryView.findViewById(R.id.layout_details);

        TextView tvYes = categoryView.findViewById(R.id.tv_content_yes);
        TextView tvNo = categoryView.findViewById(R.id.tv_content_no);
        TextView tvTip = categoryView.findViewById(R.id.tv_tip);

        // Recuperiamo il colore della categoria (es. Blu, Giallo)
        int categoryColor = ContextCompat.getColor(requireContext(), colorResId);
        // Recuperiamo il bianco per quando si chiude
        int whiteColor = ContextCompat.getColor(requireContext(), android.R.color.white);

        // 1. Imposta Dati Base
        tvTitle.setText(title);
        imgIcon.setImageResource(iconResId);

        int color = ContextCompat.getColor(requireContext(), colorResId);
        iconContainer.setBackgroundTintList(ColorStateList.valueOf(color));

        // 2. Imposta Dati Dettaglio
        tvYes.setText(tYes);
        tvNo.setText(tNo);
        tvTip.setText(tTip);

        // 3. Gestione Click (Espandi/Collassa)
        headerLayout.setOnClickListener(v -> {
            if (detailsLayout.getVisibility() == View.GONE) {
                // ESPANDI
                detailsLayout.setVisibility(View.VISIBLE);
                cardView.setCardBackgroundColor(categoryColor);
                imgArrow.animate().rotation(180).setDuration(200).start();
            } else {
                // COLLASSA
                detailsLayout.setVisibility(View.GONE);
                imgArrow.animate().rotation(0).setDuration(200).start();
                cardView.setCardBackgroundColor(whiteColor);
            }
        });
    }
}