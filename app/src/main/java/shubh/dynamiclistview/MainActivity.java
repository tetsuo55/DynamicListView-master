package shubh.dynamiclistview;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.danielfelgar.drawreceiptlib.ReceiptBuilder;
import com.mazenrashed.printooth.Printooth;
import com.mazenrashed.printooth.data.printable.ImagePrintable;
import com.mazenrashed.printooth.data.printable.Printable;
import com.mazenrashed.printooth.ui.ScanningActivity;
import com.mazenrashed.printooth.utilities.Printing;
import com.mazenrashed.printooth.utilities.PrintingCallback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

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

        if (printing != null)
            printing.setPrintingCallback(this);
        //event
        btn_unpair_pair.setOnClickListener(View -> {
            if (Printooth.INSTANCE.hasPairedPrinter())
                Printooth.INSTANCE.removeCurrentPrinter();
            else {
                startActivityForResult(new Intent(MainActivity.this, ScanningActivity.class), ScanningActivity.SCANNING_FOR_PRINTER);
                changePairAndUnpair();
            }
        });

        btn_print.setOnClickListener(view -> {
            if (!Printooth.INSTANCE.hasPairedPrinter())
                startActivityForResult(new Intent(this, ScanningActivity.class), ScanningActivity.SCANNING_FOR_PRINTER);
            else
                PrintImages();
        });

        changePairAndUnpair();

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

                    int finalprice = getApplicationContext().getResources().getIntArray(R.array.products_price)[productArrayList.indexOf(item)];

                    float price = finalprice / 100f;

                    for (int j = 0; j < productArrayList.size(); j++) {
                        if (ItemArray[j][0].contains(item)) {
                            float array_price = price + Float.parseFloat(ItemArray[j][1]);
                            int number = Integer.parseInt(ItemArray[j][2]) + 1;
                            ItemArray[j][1] = Float.toString(array_price);
                            ItemArray[j][2] = Integer.toString(number);
                        }
                    }


                    HashMap<String, Integer> items = new HashMap<>();
                    for (String newItem : product) {
                        if (!items.containsKey(newItem)) items.put(newItem, 1);
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

                float ExclBtw = totalprice / 100f * 0.91f;
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

    private void PrintImages() {
        ArrayList<Printable> printables = new ArrayList<>();

        //Load image from internet (needs to load image from ivRFeceipt instead
        Picasso.get()
                .load("http://simpleicon.com/wp-content/uploads/rocket.png")
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        printables.add(new ImagePrintable.Builder(bitmap).build());

                        printing.print(printables);

                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });

    }

    private void changePairAndUnpair() {
        if (Printooth.INSTANCE.hasPairedPrinter())
            btn_unpair_pair.setText(new StringBuilder("Unpair ")
                    .append(Printooth.INSTANCE.getPairedPrinter().getName()).toString());
        else
            btn_unpair_pair.setText("Pair with printer");
    }

    @Override
    public void connectingWithPrinter() {
        Toast.makeText(this, "Connecting to Printer", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void connectionFailed(String s) {
        Toast.makeText(this, "Failed: " + s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(String s) {
        Toast.makeText(this, "Error: " + s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMessage(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void printingOrderSentSuccessfully() {
        Toast.makeText(this, "Order sent to printer", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ScanningActivity.SCANNING_FOR_PRINTER && resultCode == Activity.RESULT_OK)
            initPrinting();
        changePairAndUnpair();
    }

    private void initPrinting() {
        if (Printooth.INSTANCE.hasPairedPrinter())
            printing = Printooth.INSTANCE.printer();
        if (printing != null)
            printing.setPrintingCallback(this);
    }
}