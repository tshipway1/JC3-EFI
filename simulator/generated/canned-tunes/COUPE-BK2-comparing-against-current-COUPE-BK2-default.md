# COUPE-BK2-comparing-against-current-COUPE-BK2-default

// canned tune https://rusefi.com/online/view.php?msq=1507

```
    // default 0.0
    engineConfiguration->cylinderBankSelect[0] = 1;
    // default 0.0
    engineConfiguration->cylinderBankSelect[1] = 1;
    // default 0.0
    engineConfiguration->cylinderBankSelect[2] = 1;
    // default 0.0
    engineConfiguration->cylinderBankSelect[3] = 1;
    // default 0.0
    engineConfiguration->cylinderBankSelect[4] = 1;
    // default 0.0
    engineConfiguration->cylinderBankSelect[5] = 1;
    // default 0.0
    engineConfiguration->cylinderBankSelect[6] = 1;
    // default 0.0
    engineConfiguration->cylinderBankSelect[7] = 1;
    // default 0.0
    engineConfiguration->cylinderBankSelect[8] = 1;
    // default 0.0
    engineConfiguration->cylinderBankSelect[9] = 1;
    // default 0.0
    engineConfiguration->cylinderBankSelect[10] = 1;
    // default 0.0
    engineConfiguration->cylinderBankSelect[11] = 1;
    // default 300.0
    engineConfiguration->idle.solenoidFrequency = 200;
    // default "false"
    engineConfiguration->stepperDcInvertedPins = true;
    // default 0.0
    engineConfiguration->idleTimingPid.dFactor = 5.0E-5;
    // default 20.0
    engineConfiguration->knockRetardAggression = 0;
    // default 3.0
    engineConfiguration->knockRetardReapplyRate = 0;
    // default "MAP"
    engineConfiguration->debugMode = DBG_22;
    // default 250.0
    engineConfiguration->etbRevLimitRange = 0;
    // default 2000.0
    engineConfiguration->boostControlMinRpm = 0;
    // default 30.0
    engineConfiguration->boostControlMinTps = 0;
    // default 110.0
    engineConfiguration->boostControlMinMap = 0;


	coupleBK2cannedtpsTpsAccelTable();
	coupleBK2cannedscriptTable4();
	coupleBK2cannedpedalToTpsTable();
	coupleBK2cannedlambdaTable();
	coupleBK2cannedtcuSolenoidTable();
	coupleBK2cannedpostCrankingFactor();
```
