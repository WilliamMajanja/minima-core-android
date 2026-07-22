package org.minimarex.minimacore.main.views.balance;

import android.app.Activity;
import android.widget.ListView;

import org.minima.utils.json.JSONArray;
import org.minima.utils.json.JSONObject;
import org.minimarex.minimacore.R;
import org.minimarex.minimacore.main.BaseView;
import org.minimarex.minimacore.utils.MinimaCMD;
import org.minimarex.minimacore.utils.MinimaCMDListener;
import org.minimarex.minimacore.utils.logger;

public class BalanceView extends BaseView {

    ListView mBalanceList;

    BalanceAdapter mBalanceAdapter;

    public BalanceView(Activity zActivity){
        super(zActivity, R.layout.view_wallet_balance);

        mBalanceAdapter = new BalanceAdapter(zActivity);

        mBalanceList = getMainView().findViewById(R.id.wallet_balance_list);
        mBalanceList.setAdapter(mBalanceAdapter);

        refreshView();
    }

    @Override
    public void refreshView() {

        //Have we started
        if(!MinimaCMD.checkMinimaStarted()){

            JSONArray tempbal = new JSONArray();
            JSONObject bal = new JSONObject();
            bal.put("tokenid","0x00");
            bal.put("token","Awaiting Connection..");
            bal.put("confirmed","0");
            bal.put("unconfirmed","0");
            tempbal.add(bal);

            refreshBalance(tempbal);

            return;
        }

        //Run Cmd
        MinimaCMD.runMinima("balance", new MinimaCMDListener() {
            @Override
            public void cmdResult(JSONObject zResult) {

                //Get the balance response
                JSONArray balance = (JSONArray)zResult.get("response");

                if(balance == null){
                    logger.log("NULL BALANCE : "+zResult.toString());
                    return;
                }

                refreshBalance(balance);
            }
        });
    }

    private void refreshBalance(JSONArray zBalance){
        mBalanceList.post(new Runnable() {
            @Override
            public void run() {
                mBalanceAdapter.updateValues(zBalance);
                mBalanceList.invalidate();
            }
        });
    }
}
