#include "pch.h"

#include "defaults.h"
#include "table_helper.h"

void setDefaultCranking() {
	engineConfiguration->cranking.rpm = 550;

	// Fuel
	engineConfiguration->crankingInjectionMode = IM_SIMULTANEOUS;
	engineConfiguration->cranking.baseFuel = 27;

	// Ignition
	engineConfiguration->ignitionDwellForCrankingMs = DEFAULT_CRANKING_DWELL_MS;
	engineConfiguration->crankingTimingAngle = DEFAULT_CRANKING_ANGLE;

	// IAC
	engineConfiguration->crankingIACposition = 50;
	engineConfiguration->afterCrankingIACtaperDuration = 200;

	engineConfiguration->isFasterEngineSpinUpEnabled = true;

	// After start enrichment
#if !EFI_UNIT_TEST
	// don't set this for unit tests, as it makes things more complicated to test
	static const float defaultPostCrankingCLTBins[] = {
		-20.0f, 0.0f, 20.0f, 40.0f, 60.0f, 80.0f
	};
	static const uint16_t defaultPostCrankinDurationBins[] = {
		0, 15, 35, 65, 100, 150
	};
	copyArray(engineConfiguration->postCrankingCLTBins, defaultPostCrankingCLTBins);
	copyArray(engineConfiguration->postCrankingDurationBins, defaultPostCrankinDurationBins);
	setTable(engineConfiguration->postCrankingFactor, 1.2f);
#endif

	setLinearCurve(config->crankingTpsCoef, /*from*/1, /*to*/1, 1);
	setLinearCurve(config->crankingTpsBins, 0, 100, 1);

	setLinearCurve(config->cltCrankingCorrBins, CLT_CURVE_RANGE_FROM, 100, 1);
	setLinearCurve(config->cltCrankingCorr, 1.0, 1.0, 1);

	// Cranking temperature compensation
	static const float crankingCoef[] = {
		2.8,
		2.2,
		1.8,
		1.55,
		1.3,
		1.1,
		1.0,
		1.0
	};
	copyArray(config->crankingFuelCoef,     crankingCoef);
	copyArray(config->crankingFuelCoefE100, crankingCoef);

	// Deg C
	static const float crankingBins[] = {
		-20,
		-10,
		5,
		20,
		35,
		50,
		65,
		90
	};
	copyArray(config->crankingFuelBins, crankingBins);

	// Cranking cycle compensation

	// Whole table is 1.0, except first two values are steeper
	setArrayValues(config->crankingCycleCoef, 1.0f);
	config->crankingCycleCoef[0] = 2.0f;
	config->crankingCycleCoef[1] = 1.3f;

	// X values are simply counting up cycle number starting at 1
	for (size_t i = 0; i < efi::size(config->crankingCycleBins); i++) {
		config->crankingCycleBins[i] = i + 1;
	}

	// Cranking ignition timing
	setArrayValues(config->crankingAdvance, 0);

	static const float advanceBins[] = { 0, 200, 400, 1000 };
	copyArray(config->crankingAdvanceBins, advanceBins);

	engineConfiguration->useTLE8888_cranking_hack = true;
}
