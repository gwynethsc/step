// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * FindMeetingQuery contains a query method that finds the best possible open meeting times, if any,
 * given existing events and a request
 */
public final class FindMeetingQuery {

    private enum AvailabilityChangeType {
        START, END
    }

    private static class AvailabilityChange {
        private final AvailabilityChangeType type;
        private final int timestamp;
        private final Set<String> attendees;

        public AvailabilityChange(AvailabilityChangeType type, TimeRange when, Collection<String> attendees) {
            if (when == null) {
                throw new IllegalArgumentException("when cannot be null.");
            }

            if (attendees == null) {
                this.attendees = Collections.unmodifiableSet(new HashSet<>());
            } else {
                this.attendees = Collections.unmodifiableSet(new HashSet<>(attendees));
            }

            this.type = type;
            if (type == AvailabilityChangeType.START) {
                this.timestamp = when.start();
            }
            else {
                this.timestamp = when.end();
            }
        }

        public AvailabilityChangeType type() {
            return type;
        }

        public int timestamp() {
            return timestamp;
        }

        public Set<String> getAttendees() {
            return attendees;
        }

        public static final Comparator<AvailabilityChange> ORDER = 
            (AvailabilityChange a, AvailabilityChange b) -> Long.compare(a.timestamp, b.timestamp);
    }

    /**
     * Given a collection of existing events and a meeting request, returns a list of all
     * time ranges with at least the duration of the request in which all mandatory attendees 
     * are available
     *
     * @param events a collection of preexisting meeting events
     * @param request specifies the meeting to be scheduled
     *
     * @return all time ranges that are sufficiently long for the meeting request and during which all
     *         mandatory attendees are available
     */
    public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
        Collection<TimeRange> withOptional = query(events, request, true);
        if (!withOptional.isEmpty() || request.getAttendees().isEmpty()) {
            return withOptional;
        }
        return query(events, request, false);
    }

    /**
     * query with additional parameter on whether or not to consider optional attendees
     */
    private Collection<TimeRange> query(Collection<Event> events, MeetingRequest request, boolean considerOptionalAttendees) {
        Set<String> consideredAttendees = new HashSet<String>(request.getAttendees());
        if (considerOptionalAttendees) {
            consideredAttendees.addAll(request.getOptionalAttendees());
        }

        List<AvailabilityChange> changes = getChangesInAvailabilityFromEvents(events);

        Set<String> currentAttendees = new HashSet<String>();
        List<TimeRange> meetingOptions = new ArrayList<TimeRange>();
        int slotStart = 0;
        for (AvailabilityChange change : changes) {
            applyAvailabilityChange(currentAttendees, change, consideredAttendees);
            
            // Use change information to open or close a new potential slot, as applicable
            if (slotStart >= 0 && !currentAttendees.isEmpty()) {
                addMeetingOption(meetingOptions, slotStart, change.timestamp(), request.getDuration());
                slotStart = -1;
            } else if (slotStart < 0 && currentAttendees.isEmpty()) {
                slotStart = change.timestamp();
            }
        }

        // Add end-of-day slot, if applicable
        if (slotStart >= 0) {
            addMeetingOption(meetingOptions, slotStart, TimeRange.END_OF_DAY + 1, request.getDuration());
        }

        return meetingOptions;
    }

    private List<AvailabilityChange> getChangesInAvailabilityFromEvents(Collection<Event> events) {
        List<AvailabilityChange> changes = new ArrayList<AvailabilityChange>();
        for (Event event : events) {
            changes.add(new AvailabilityChange(AvailabilityChangeType.START, event.getWhen(), event.getAttendees()));
            changes.add(new AvailabilityChange(AvailabilityChangeType.END, event.getWhen(), event.getAttendees()));
        }
        Collections.sort(changes, AvailabilityChange.ORDER);
        return changes;
    }

    /**
     * In-place modification of a set of attendees to reflect a change in availability change
     *
     * @param currentAttendees
     * @param change
     * @param consideredAttendees
     */
    private void applyAvailabilityChange(Set<String> currentAttendees, AvailabilityChange change, Collection<String> consideredAttendees) {
        Set<String> changeAttendees = new HashSet<String>(consideredAttendees);
        changeAttendees.retainAll(change.getAttendees());

        if (change.type() == AvailabilityChangeType.START) {
            currentAttendees.addAll(changeAttendees);
        } else {
            currentAttendees.removeAll(changeAttendees);
        }
    }

    /**
     * Given a start time and end time when attendees are available, determines if the availability is long enough and if so,
     * adds it to a running list of meetingOptions in-place
     *
     * @param meetingOptions
     * @param start
     * @param end
     * @param request
     */
    private void addMeetingOption(List<TimeRange> meetingOptions, int start, int end, long durationThreshold) {
        TimeRange openSlot = TimeRange.fromStartEnd(start, end, false);
        if (openSlot.duration() >= durationThreshold)
            meetingOptions.add(openSlot);
    }
}
