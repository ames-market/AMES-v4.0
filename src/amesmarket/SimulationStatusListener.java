package amesmarket;

public interface SimulationStatusListener {

    public void receiveStatusEvent(StatusEvent evt);

    public static class StatusEvent {
        public static final int UPDATE_HOUR = 1;
        public static final int UPDATE_DAY = 2;
        public static final int UPDATE_ZONE_COUNT = 3;
        public static final int UPDATE_GENCO_COUNT = 4;
        public static final int UPDATE_LSE_COUNT = 5;
        public static final int UPDATE_LMPS = 6;
        public static final int UPDATE_BRANCH_FLOW = 7;
        public static final int UPDATE_HAS_SOLUTION = 8;
        public static final int UPDATE_COMMITMENTS = 9;
        public static final int UPDATE_RT_LOAD     = 10;
        public static final int UPDATE_LSE_DEMAND = 11;
        public static final int UPDATE_COSTS = 12;

        //FIXME: Type safety issues.s
        public final int eventType;
        /**
         * The 'new' value from this event. May be null.
         */
        public final Object value;
        /**
         * The object that fired the event.
         */
        public final Object src;
        public StatusEvent(int eventType, Object value, Object src) {
            this.eventType = eventType;
            this.value = value;
            this.src = src;
        }
    }
}
