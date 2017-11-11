package info.papdt.blackblub;

public final class Constants {

    public static final String NOTIFICATION_CHANNEL_ID_RS = "running_status";

    public static final String ACTION_UPDATE_STATUS = "info.papdt.blackbulb.ACTION_UPDATE_STATUS";
    public static final String ACTION_ALARM_START = "info.papdt.blackbulb.ALARM_ACTION_START";
    public static final String ACTION_ALARM_STOP = "info.papdt.blackbulb.ALARM_ACTION_STOP";
    public static final String ACTION_TOGGLE = "info.papdt.blackbulb.ACTION_TOGGLE";

    public static final class AdvancedMode {

        public static final int NONE = 0;
        public static final int NO_PERMISSION = 1;
        public static final int OVERLAY_ALL = 2;

    }

    public static final class Extra {

        public static final String EVENT_ID = "event_id";
        public static final String ACTION = "action_name";
        public static final String BRIGHTNESS = "brightness";
        public static final String ADVANCED_MODE = "advanced_mode";
        public static final String IS_SHOWING = "is_showing";
        public static final String YELLOW_FILTER_ALPHA = "yellow_filter_alpha";

    }

    public static final class Action {

        public static final int START = 1;
        public static final int UPDATE = 2;
        public static final int PAUSE = 3;
        public static final int STOP = 4;
        public static final int CHECK = 5;

    }

    public static final class Event {

        public static final int CANNOT_START = 1;
        public static final int DESTROY_SERVICE = 2;
        public static final int CHECK = 3;

    }

}
