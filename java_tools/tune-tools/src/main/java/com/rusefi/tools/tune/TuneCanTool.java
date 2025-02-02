package com.rusefi.tools.tune;

import com.devexperts.logging.Logging;
import com.opensr5.ini.DialogModel;
import com.opensr5.ini.IniFileModel;
import com.rusefi.*;
import com.rusefi.core.preferences.storage.Node;
import com.rusefi.enums.engine_type_e;
import com.rusefi.parse.TypesHelper;
import com.rusefi.tune.xml.Constant;
import com.rusefi.tune.xml.Msq;
import com.rusefi.xml.XmlUtil;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static com.devexperts.logging.Logging.getLogging;
import static com.rusefi.ConfigFieldImpl.unquote;
import static com.rusefi.config.Field.niceToString;

/**
 * this command line utility compares two TS calibration files and produces .md files with C++ source code of the difference between those two files.
 * <p>
 * Base 'default settings' file is msq generated by WriteSimulatorConfiguration.java with .xml extension but a real .msq could probably be used instead.
 * Second calibration file which contains desired base calibrations is a native TS calibration file.
 * <p>
 * [CannedTunes]
 * <p>
 * see <a href="https://github.com/rusefi/rusefi/wiki/Canned-Tune-Process">...</a>
 */
public class TuneCanTool implements TuneCanToolConstants {
    private static final Logging log = getLogging(TuneCanTool.class);
    private static final String REPORTS_OUTPUT_FOLDER = "generated/canned-tunes";

    private static final String FOLDER = "generated";
    public static final String SIMULATED_PREFIX = FOLDER + File.separator + "simulator_tune";
    public static final String TUNE_FILE_SUFFIX = ".msq";
    public static final String DEFAULT_TUNE = SIMULATED_PREFIX + TUNE_FILE_SUFFIX;
    private static final String workingFolder = "downloaded_tunes";
    public static final String MD_FIXED_FORMATTING = "```\n";
    // IDE and GHA run from different working folders :(
    public static final String YET_ANOTHER_ROOT = "../simulator/";

    protected static IniFileModel ini;


    public static void main(String[] args) throws Exception {
        writeDiffBetweenLocalTuneFileAndDefaultTune("../1.msq");

//        writeDiffBetweenLocalTuneFileAndDefaultTune("vehicleName", getDefaultTuneName(Fields.engine_type_e_MAVERICK_X3),
//            "C:\\stuff\\i\\canam-2022-short\\canam-progress-pnp-dec-29.msq",  "comment");


//        handle("Mitsubicha", 1258);
//        handle("Scion-1NZ-FE", 1448);
//        handle("4g93", 1425);
//        handle("BMW-mtmotorsport", 1479);
    }

    /**
     * @see WriteSimulatorConfiguration
     */
    protected static void processREOtune(int tuneId, engine_type_e engineType, String key,
                                       String methodNamePrefix) throws JAXBException, IOException {
        // compare specific internet tune to total global default
        handle(key + "-comparing-against-global-defaults", tuneId, YET_ANOTHER_ROOT + TuneCanTool.DEFAULT_TUNE, methodNamePrefix);
        // compare same internet tune to default tune of specified engine type
        handle(key + "-comparing-against-current-" + key + "-default", tuneId, getDefaultTuneName(engineType), methodNamePrefix);
    }

    @NotNull
    public static String getDefaultTuneName(engine_type_e engineType) {
        return YET_ANOTHER_ROOT + SIMULATED_PREFIX + "_" + engineType.name() + TUNE_FILE_SUFFIX;
    }

    private static void handle(String vehicleName, int tuneId, String defaultTuneFileName, String methodNamePrefix) throws JAXBException, IOException {
        String customTuneFileName = workingFolder + File.separator + tuneId + ".msq";
        String url = "https://rusefi.com/online/view.php?msq=" + tuneId;

        downloadTune(tuneId, customTuneFileName);

        writeDiffBetweenLocalTuneFileAndDefaultTune(vehicleName, defaultTuneFileName, customTuneFileName, url, methodNamePrefix);
    }

    private static void writeDiffBetweenLocalTuneFileAndDefaultTune(String localFileName) throws JAXBException, IOException {
        writeDiffBetweenLocalTuneFileAndDefaultTune("vehicleName", DEFAULT_TUNE,
            localFileName,  "comment", "");
    }
//
//    private static void writeDiffBetweenLocalTuneFileAndDefaultTune(int engineCode, String localFileName, String cannedComment) throws JAXBException, IOException {
//        writeDiffBetweenLocalTuneFileAndDefaultTune("vehicleName", getDefaultTuneName(engineCode),
//            localFileName,  cannedComment);
//    }

    private static void writeDiffBetweenLocalTuneFileAndDefaultTune(String vehicleName, String defaultTuneFileName, String customTuneFileName, String cannedComment, String methodNamePrefix) throws JAXBException, IOException {
        new File(REPORTS_OUTPUT_FOLDER).mkdir();

        Msq customTune = Msq.readTune(customTuneFileName);
        File xmlFile = new File(defaultTuneFileName);
        log.info("Reading " + xmlFile.getAbsolutePath());
        Msq defaultTune = XmlUtil.readModel(Msq.class, xmlFile);

        StringBuilder methods = new StringBuilder();

        StringBuilder sb = getTunePatch(defaultTune, customTune, ini, customTuneFileName, methods, defaultTuneFileName, methodNamePrefix);

        String fileNameMethods = YET_ANOTHER_ROOT + REPORTS_OUTPUT_FOLDER + "/" + vehicleName + "_methods.md";
        try (FileWriter methodsWriter = new FileWriter(fileNameMethods)) {
            methodsWriter.append(MD_FIXED_FORMATTING);
            methodsWriter.append(methods);
            methodsWriter.append(MD_FIXED_FORMATTING);
        }

        String fileName = YET_ANOTHER_ROOT + REPORTS_OUTPUT_FOLDER + "/" + vehicleName + ".md";
        File outputFile = new File(fileName);
        log.info("Writing to " + outputFile.getAbsolutePath());

        try (FileWriter w = new FileWriter(outputFile)) {
            w.append("# " + vehicleName + "\n\n");
            w.append("// canned tune " + cannedComment + "\n\n");

            w.append(MD_FIXED_FORMATTING);
            w.append(sb);
            w.append(MD_FIXED_FORMATTING);
        }
        log.info("Done writing to " + outputFile.getAbsolutePath() + "!");
    }

    private static void downloadTune(int tuneId, String localFileName) throws IOException {
        new File(workingFolder).mkdirs();
        String downloadUrl = "https://rusefi.com/online/download.php?msq=" + tuneId;
        InputStream in = new URL(downloadUrl).openStream();
        Files.copy(in, Paths.get(localFileName), StandardCopyOption.REPLACE_EXISTING);
    }

    private static boolean isHardwareEnum(String type) {
        switch (type) {
            case "output_pin_e":
            case "brain_input_pin_e":
            case "adc_channel_e":
            case "Gpio":
            case "spi_device_e":
            case "pin_input_mode_e":
            case "pin_output_mode_e":
                return true;
        }
        return false;
    }

    private static Object simplerSpaces(String value) {
        if (value == null)
            return value;
        return value.replaceAll("\\s+", " ").trim();
    }

    @NotNull
    public static StringBuilder getTunePatch(Msq defaultTune, Msq customTune, IniFileModel ini, String customTuneFileName, StringBuilder methods, String defaultTuneFileName, String methodNamePrefix) throws IOException {
        ReaderStateImpl state = MetaHelper.getReaderState();

        StringBuilder invokeMethods = new StringBuilder();


        StringBuilder sb = new StringBuilder();
        for (DialogModel.Field f : ini.fieldsInUiOrder.values()) {
            String fieldName = f.getKey();
//            System.out.println("Processing " + fieldName);
            Constant customValue = customTune.getConstantsAsMap().get(fieldName);
            Constant defaultValue = defaultTune.getConstantsAsMap().get(fieldName);
            if (defaultValue == null) {
                // no longer present?
                System.out.println("Not found in default tune: " + fieldName);
                continue;
            }
            Objects.requireNonNull(defaultValue.getValue(), "d value");
            if (customValue == null) {
                log.info("Skipping " + fieldName + " not present in tune");
                continue;
            }
            Objects.requireNonNull(customValue.getValue(), "c value");

            boolean isSameValue = simplerSpaces(defaultValue.getValue()).equals(simplerSpaces(customValue.getValue()));
            if (isSameValue) {
                System.out.println("Even text form matches default: " + fieldName);
                continue;
            }

            // todo: what about stuff outside of engine_configuration_s?
            StringBuffer context = new StringBuffer();

            ConfigField cf = MetaHelper.findField(state, fieldName, context);
            if (cf == null) {
                log.info("Not found " + fieldName);
                continue;
            }
            if (TypesHelper.isFloat(cf.getType()) && !cf.isArray()) {
                float floatDefaultValue = Float.parseFloat(defaultValue.getValue());
                float floatCustomValue = Float.parseFloat(customValue.getValue());
                if (floatCustomValue != 0 && Math.abs(floatDefaultValue / floatCustomValue - 1) < 0.001) {
                    System.out.println("Skipping rounding error " + floatDefaultValue + " vs " + floatCustomValue);
                    continue;
                }
            }
            String cName = context + cf.getOriginalArrayName();

            if (isHardwareProperty(cf.getName())) {
                continue;
            }

            if (cf.getType().equals("boolean")) {
                sb.append(TuneTools.getAssignmentCode(defaultValue, cName, unquote(customValue.getValue())));
                continue;
            }

            if (cf.isArray()) {
                String parentReference;
                if (cf.getParent().getName().equals("engine_configuration_s")) {
                    parentReference = "engineConfiguration->";
                } else if (cf.getParent().getName().equals("persistent_config_s")) {
                    parentReference = "config->";
                } else {
                    // todo: for instance map.samplingAngle
                    //throw new IllegalStateException("Unexpected " + cf.getParent());
                    System.out.println(" " + cf);
                    continue;
                }


                if (cf.getArraySizes().length == 2) {
                    TableData tableData = TableData.readTable(customTuneFileName, fieldName, ini);
                    if (tableData == null) {
                        System.out.println(" " + fieldName);
                        continue;
                    }
                    System.out.println("Handling table " + fieldName + " with " + cf.autoscaleSpecPair());

                    if (defaultTuneFileName != null) {
                        TableData defaultTableData = TableData.readTable(defaultTuneFileName, fieldName, ini);
                        if (defaultTableData.getCsourceMethod(parentReference, methodNamePrefix).equals(tableData.getCsourceMethod(parentReference, methodNamePrefix))) {
                            System.out.println("Table " + fieldName + " matches default content");
                            continue;
                        }
                    }
                    System.out.println("Custom content in table " + fieldName);


                    methods.append(tableData.getCsourceMethod(parentReference, methodNamePrefix));
                    invokeMethods.append(tableData.getCinvokeMethod(methodNamePrefix));
                    continue;
                }

                CurveData data = CurveData.valueOf(customTuneFileName, fieldName, ini);
                if (data == null)
                    continue;

                if (defaultTuneFileName != null) {
                    CurveData defaultCurveData = CurveData.valueOf(defaultTuneFileName, fieldName, ini);
                    if (defaultCurveData.getCinvokeMethod(methodNamePrefix).equals(data.getCinvokeMethod(methodNamePrefix))) {
                        System.out.println("Curve " + fieldName + " matches default content");
                        continue;
                    }
                }
                System.out.println("Custom content in curve " + fieldName);

                methods.append(data.getCsourceMethod(parentReference, methodNamePrefix));
                invokeMethods.append(data.getCinvokeMethod(methodNamePrefix));

                continue;
            }

            if (!Node.isNumeric(customValue.getValue())) {
                // todo: smarter logic for enums

                String type = cf.getType();
                if (isHardwareEnum(type)) {
                    continue;
                }
                EnumsReader.EnumState sourceCodeEnum = state.getEnumsReader().getEnums().get(type);
                if (sourceCodeEnum == null) {
                    log.info("No info for " + type);
                    continue;
                }
                String customEnum = state.getTsCustomLine().get(type);

                int ordinal;
                try {
                    ordinal = TuneTools.resolveEnumByName(customEnum, unquote(customValue.getValue()));
                } catch (IllegalStateException e) {
                    log.info("Looks like things were renamed: " + customValue.getValue() + " not found in " + customEnum);
                    continue;
                }

                log.info(cf + " " + sourceCodeEnum + " " + customEnum + " " + ordinal);

                String sourceCodeValue = sourceCodeEnum.findByValue(ordinal);
                sb.append(TuneTools.getAssignmentCode(defaultValue, cName, sourceCodeValue));

                continue;
            }
            double doubleValue = Double.valueOf(customValue.getValue());
            int intValue = (int) doubleValue;
            boolean isInteger = intValue == doubleValue;
            if (isInteger) {
                sb.append(TuneTools.getAssignmentCode(defaultValue, cName, Integer.toString(intValue)));
            } else {
                sb.append(TuneTools.getAssignmentCode(defaultValue, cName, niceToString(doubleValue)));
            }

        }
        sb.append("\n\n").append(invokeMethods);

        return sb;
    }

    private final static Set<String> HARDWARE_PROPERTIES = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    static {
        HARDWARE_PROPERTIES.addAll(Arrays.asList(
            "invertPrimaryTriggerSignal",
            "invertSecondaryTriggerSignal",
            "invertCamVVTSignal",
            "adcVcc",
            "vbattDividerCoeff",
            "warningPeriod", // inconsistency between prod code and simulator
            "engineChartSize", // inconsistency between prod code and simulator
            "displayLogicLevelsInEngineSniffer",
            "isSdCardEnabled",
            "is_enabled_spi_1",
            "is_enabled_spi_2",
            "is_enabled_spi_3"
        ));
    }

    private static boolean isHardwareProperty(String name) {
        return HARDWARE_PROPERTIES.contains(name);
    }
}
