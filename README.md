# minima-core-android
An Android application running minima-core 

This is a simple clean Minima client that runs in full on Android

Fully non-custodial with wallet functionality

You also have the miniaapi.aar - an Android lib that allows your own applications to talk to Minima Core.

Simply load the minimaapi.aar module/lib into your project.

The main API is accessed via
```
        //You must first register to allow messages to be sent and push notifications
        mMinimaAPI = new MinimaAPI(this, new MinimaAPIListener() {
            @Override
            public void response(JSONObject zResponse) {
                MinimaAPILogger.log(zResponse.toString());
            }
        });

        //Run a Minima command
        mMinimaAPI.Command("block", new MinimaAPIListener() {
            @Override
            public void response(JSONObject zResponse) {
                
                //You can now use the JSON zResponse object..
                //..
                        
                //If you want to update a UI component run it on the UI Thread..
                //MainActivity.this.runOnUiThread(new Runnable() {
                //    @Override
                //    public void run() {}
                //});
            }
        });
```

The application shows up in Minima-Core and must be enabled by the User

You MUST call onDestroy from the MinimaAPI to shutdown cleanly
```
    @Override
    protected void onDestroy() {
        super.onDestroy();

        mMinimaAPI.onDestroy();
    }
```

You can receive push notifications of Minima events by creating a BroadcastReceiver in your app and listening for
```
org.minimarex.minimacore.NOTIFY
```

Look at the Terminal APK for an example of how this works..

