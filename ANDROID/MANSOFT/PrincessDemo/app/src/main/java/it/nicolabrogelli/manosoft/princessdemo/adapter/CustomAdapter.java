package it.nicolabrogelli.manosoft.princessdemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import it.nicolabrogelli.manosoft.princessdemo.R;
import it.nicolabrogelli.manosoft.princessdemo.model.ElementoLista;


/**
 * Created by Nicola on 05/10/2017.
 */

public class CustomAdapter extends ArrayAdapter<ElementoLista> {

    public CustomAdapter(Context context, int textViewResourceId, List<ElementoLista> objects) {
        super(context, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.row, null);

        TextView titolo = (TextView)convertView.findViewById(R.id.titolo);
        TextView descrizione = (TextView)convertView.findViewById(R.id.descrizione);
        ImageView immagine = (ImageView)convertView.findViewById(R.id.icon);

        ElementoLista c = getItem(position);
        if (c != null) {
            titolo.setText("Caprone");
            descrizione.setText(c.getDescrizione());
            if(c.getTipo().equals("(D)")) {
                immagine.setImageResource(R.drawable.ic_folder);
            } else {
                immagine.setImageResource(R.drawable.ic_file);
            }
        }


        return convertView;
    }

}