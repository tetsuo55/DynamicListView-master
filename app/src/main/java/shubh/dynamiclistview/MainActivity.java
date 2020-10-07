package shubh.dynamiclistview;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;

import com.github.danielfelgar.drawreceiptlib.ReceiptBuilder;
import com.mazenrashed.printooth.utilities.Printing;
import com.mazenrashed.printooth.utilities.PrintingCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends Activity implements PrintingCallback {

    ListView listview;
    ImageView ivReceipt;
    Button Addbutton;
    Button generateButton;
    Button btn_unpair_pair;
    Button btn_print;
    Printing printing;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        listview = (ListView) findViewById(R.id.listView1);
        Addbutton = (Button) findViewById(R.id.button);
        generateButton = (Button) findViewById(R.id.button1);
        ivReceipt = findViewById(R.id.ivReceipt);
        btn_unpair_pair = (Button) findViewById(R.id.BtnPairUnpair);
        btn_print = (Button) findViewById(R.id.BtnPrint);

        if(printing !=null)
                printing.setPrintingCallback(this);
        //event
        btn_unpair_pair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        final String date = String.valueOf(android.text.format.DateFormat.format("dd-MM-yyyy", new java.util.Date()));

            /*
            Set spinner
             */
        final Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> spinneradapter = ArrayAdapter.createFromResource(
                this, R.array.products, android.R.layout.simple_spinner_item);
        spinneradapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinneradapter);

        /*
        Set product prices array
         */

        String[] prices = getApplicationContext().getResources().getStringArray(R.array.products_price);
        final List<String> pricesArrayList = Arrays.asList(prices);

        /*
        Set product array
         */

        final String[] product = getApplicationContext().getResources().getStringArray(R.array.products);
        final List<String> productArrayList = Arrays.asList(product);


        final List<String> ListElementsArrayList = new ArrayList<>();


        final ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (MainActivity.this, android.R.layout.simple_list_item_1, ListElementsArrayList);

        listview.setAdapter(adapter);

        Addbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ListElementsArrayList.add(spinner.getSelectedItem().toString());

                adapter.notifyDataSetChanged();
            }
        });

        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap barcode = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.barcode);


                ReceiptBuilder receipt = new ReceiptBuilder(1200);
                receipt.setMargin(30, 20).
                        setAlign(Paint.Align.CENTER).
                        setColor(Color.BLACK).
                        addImage(barcode).
                        setTextSize(60).
                        setTypeface(getApplicationContext(), "fonts/RobotoMono-Regular.ttf").
                        addText("my store").
                        addText("Tel: 000-1234567").
                        addBlankSpace(30).
                        setAlign(Paint.Align.LEFT).
                        addText("Tafel: 2", false).
                        setAlign(Paint.Align.RIGHT).
                        addText(date).
                        setAlign(Paint.Align.LEFT).
                        addLine().
                        addParagraph().
                        addParagraph().
                        setTypeface(getApplicationContext(), "fonts/RobotoMono-Bold.ttf").
                        addBlankSpace(30);


                /**
                 * Loop through the listview and append
                 */

                receipt.setTypeface(getApplicationContext(), "fonts/RobotoMono-Regular.ttf");
                int totalprice = 0;

                String[][] ItemArray = new String[productArrayList.size()][3];

                for (int i = 0; i < productArrayList.size(); i++) {
                    ItemArray[i][0] = productArrayList.get(i);
                    ItemArray[i][1] = "0";
                    ItemArray[i][2] = "0";
                }

                for (int i = 0; i < listview.getCount(); i++) {
                    String item = ListElementsArrayList.get(i);

                    int finalprice =  getApplicationContext().getResources().getIntArray(R.array.products_price)[productArrayList.indexOf(item)];

                    float price = finalprice / 100f;

                    for (int j = 0; j < productArrayList.size(); j++) {
                        if (ItemArray[j][0].contains(item)) {
                            float array_price = price + Float.parseFloat(ItemArray[j][1]);
                            int number = Integer.parseInt(ItemArray[j][2]) + 1;
                            ItemArray[j][1] = Float.toString(array_price);
                            ItemArray[j][2] = Integer.toString(number);
                        }
                    }


                    HashMap<String, Integer> items  = new HashMap<>();
                    for(String newItem: product) {
                        if(!items.containsKey(newItem)) items.put(newItem, 1);
                        else items.put(newItem, items.get(newItem) + 1);
                    }
                    totalprice += finalprice;
                }

                for (int i = 0; i < productArrayList.size(); i++) {
                    if (Float.parseFloat(ItemArray[i][1]) != 0) {
                        receipt.setAlign(Paint.Align.LEFT).
                                addText(ItemArray[i][2] + "x" + ItemArray[i][0], false).
                                setAlign(Paint.Align.RIGHT).
                        addText("€" + String.format("%.2f", Float.parseFloat(ItemArray[i][1])));
                    }
                }

                float ExclBtw = totalprice / 100f * 0.91f ;
                float BTW = totalprice / 100f * 0.09f;
                float totalpricefloat = totalprice / 100f;
                receipt.setTypeface(getApplicationContext(), "fonts/RobotoMono-Regular.ttf").
                        setAlign(Paint.Align.LEFT).
                        addParagraph().
                        setTypeface(getApplicationContext(), "fonts/RobotoMono-Bold.ttf").
                        addText("Excl.", false).
                        setAlign(Paint.Align.RIGHT).
                        addText("€" + String.format("%.2f", ExclBtw)).
                        setAlign(Paint.Align.LEFT).
                        addParagraph().
                        addText("BTW", false).
                        setAlign(Paint.Align.RIGHT).
                        addText("€" + String.format("%.2f", BTW)).
                        setAlign(Paint.Align.LEFT).
                        addParagraph().
                        setTypeface(getApplicationContext(), "fonts/RobotoMono-Bold.ttf").
                        addText("Totaal", false).
                        setAlign(Paint.Align.RIGHT).
                        addText("€" + String.format("%.2f", totalpricefloat)).
                        setAlign(Paint.Align.LEFT).
                        addLine(180).
                        addParagraph().
                        setAlign(Paint.Align.CENTER).
                        setTypeface(getApplicationContext(), "fonts/RobotoMono-Regular.ttf").
                        addParagraph();
                ivReceipt.setImageBitmap(receipt.build());
            }
        });
    }

    @Override
    public void connectingWithPrinter() {

    }

    @Override
    public void connectionFailed(String s) {

    }

    @Override
    public void onError(String s) {

    }

    @Override
    public void onMessage(String s) {

    }

    @Override
    public void printingOrderSentSuccessfully() {

    }
}