package org.minimarex.minimacore.main.views.home;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.minima.database.MinimaDB;
import org.minima.objects.TxPoW;
import org.minima.system.params.GlobalParams;
import org.minima.utils.json.JSONObject;
import org.minimarex.minimacore.R;
import org.minimarex.minimacore.utils.MinimaCMD;
import org.minimarex.minimacore.utils.MinimaCMDListener;
import org.minimarex.minimacore.utils.logger;
import org.minimarex.minimacore.main.BaseView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

public class HomeView extends BaseView {

    TextView mKeyUses;
    TextView mVersion;
    TextView mMinima;
    TextView mBlock;
    TextView mBlockTime;
    TextView mConnections;
    TextView mPeers;

    private SimpleDateFormat DATEFORMAT = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.ENGLISH);

    public HomeView(Activity zActivity){
        super(zActivity, R.layout.view_home);

        mKeyUses    = getMainView().findViewById(R.id.home_keyuses);
        mMinima    = getMainView().findViewById(R.id.home_version);
        mBlock      = getMainView().findViewById(R.id.home_blocks);
        mBlockTime  = getMainView().findViewById(R.id.home_block_time);
        mConnections  = getMainView().findViewById(R.id.home_connections);
        mPeers      = getMainView().findViewById(R.id.home_peers);
        mVersion    = getMainView().findViewById(R.id.home_app_version);

        Button tester = getMainView().findViewById(R.id.button_tester);
        tester.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logger.log("Test Button pressed!");

                Intent intent = new Intent("com.example.snippets.ACTION_UPDATE_DATA");
                intent.putExtra("com.example.snippets.DATA", "SOME DATA!");
                intent.setPackage("org.minimarex.minimacore");
                zActivity.sendBroadcast(intent);
            }
        });

        refreshView();
    }

    @Override
    public void refreshView(){

        //Have we started
        if(!MinimaCMD.checkMinimaStarted()){
            return;
        }

        //Run some Minima Commands..
        MinimaCMD.runMinima("keys", new MinimaCMDListener() {
            @Override
            public void cmdResult(JSONObject zResult) {
                mKeyUses.post(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            JSONObject resp = (JSONObject) zResult.get("response");
                            int maxuses     = ((Number) resp.get("maxuses")).intValue();
                            mKeyUses.setText(""+maxuses);
                        }catch(Exception exc){
                            logger.log("Key uses error: "+exc);
                        }
                    }
                });
            }
        });

        MinimaCMD.runMinima("peers", new MinimaCMDListener() {
            @Override
            public void cmdResult(JSONObject zResult) {
                mPeers.post(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            JSONObject resp     = (JSONObject) zResult.get("response");
                            String peerslist    = resp.getString("peerslist");

                            ArrayList<String> allpeers = new ArrayList<>();
                            StringTokenizer strtok = new StringTokenizer(peerslist,",");
                            while(strtok.hasMoreTokens()){
                                allpeers.add(strtok.nextToken());
                            }

                            //Pick a random peer
                            int count       = allpeers.size();
                            Random rand     = new Random();
                            String peer1    = allpeers.get(rand.nextInt(count));

                            mPeers.setText(peer1);
                        }catch(Exception exc){
                            logger.log("Peers error: "+exc);
                        }
                    }
                });
            }
        });

        MinimaCMD.runMinima("network", new MinimaCMDListener() {
            @Override
            public void cmdResult(JSONObject zResult) {
                mConnections.post(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            JSONObject resp     = (JSONObject) zResult.get("response");
                            JSONObject details  = (JSONObject) resp.get("details");
                            int connected       = ((Number)details.get("connected")).intValue();
                            mConnections.setText(""+connected);

                        }catch(Exception exc){
                            logger.log("Network error: "+exc);
                        }
                    }
                });
            }
        });

        mMinima.setText(GlobalParams.getFullMicroVersion());

        //Get tip
        try {
            TxPoW txp = MinimaDB.getDB().getTxPoWTree().getTip().getTxPoW();
            if (txp == null) {
                return;
            }

            int block = txp.getBlockNumber().getAsInt();
            mBlock.setText(""+block);

            long timemilli  = txp.getTimeMilli().getAsLong();
            String datestr  = DATEFORMAT.format(new Date(timemilli));
            mBlockTime.setText(datestr);
        } catch (Exception e) {
            logger.log("Error getting tip: " + e);
        }

        //logger.log("HOME TIME : "+datestr);

        try {
            String vcode  = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
            mVersion.setText(vcode);
        } catch (Exception e) {
            logger.log("Error getting version: " + e);
        }

    }
}
