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

public final class FindMeetingQuery {

    public enum AvailabilityChangeType {
        START, END
    }

    private static class AvailabilityChange {
        private final AvailabilityChangeType type;
        private final int point;
        private final Set<String> attendees = new HashSet<>();

        public AvailabilityChange(AvailabilityChangeType type, TimeRange when, Collection<String> attendees) {
            if (when == null) {
              throw new IllegalArgumentException("when cannot be null.");
            }
            if (attendees == null) {
              throw new IllegalArgumentException("attendees cannot be null. Use empty array instead.");
            }
            this.type = type;
            if (type == AvailabilityChangeType.START) {
                this.point = when.start();
            }
            else {
                this.point = when.end();
            }
            this.attendees.addAll(attendees);
        }

        public AvailabilityChangeType type() {
            return type;
        }

        public int point() {
            return point;
        }

        public Set<String> getAttendees() {
            return Collections.unmodifiableSet(attendees);
        }

        public static final Comparator<AvailabilityChange> ORDER = new Comparator<AvailabilityChange>() {
            @Override
            public int compare(AvailabilityChange a, AvailabilityChange b) {
              return Long.compare(a.point, b.point);
            }
        };
    }

    /**
     * Given a collection of existing events and a meeting request, returns a list of all
     * time ranges with at least the duration of the request in which all mandatory attendees 
     * are available
     */
    public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

        // Convert existing events to a list of availability changes
        List<AvailabilityChange> changes = new ArrayList<AvailabilityChange>();
        for (Event event : events) {
            changes.add(new AvailabilityChange(AvailabilityChangeType.START, event.getWhen(), event.getAttendees()));
            changes.add(new AvailabilityChange(AvailabilityChangeType.END, event.getWhen(), event.getAttendees()));
        }
        Collections.sort(changes, AvailabilityChange.ORDER);

        Set<String> occupied = new HashSet<String>();
        List<TimeRange> openings = new ArrayList<TimeRange>();
        int slotStart = 0;
        for (AvailabilityChange change : changes) {

            Set<String> mandatoryAttendees = new HashSet<String>(request.getAttendees());
            mandatoryAttendees.retainAll(change.getAttendees());
            
            if (change.type() == AvailabilityChangeType.START) {
                occupied.addAll(mandatoryAttendees);
            } else {
                occupied.removeAll(mandatoryAttendees);
            }
            
            // Use change information to open or close a new potential slot, if applicable
            if (slotStart >= 0 && !occupied.isEmpty()) {
                TimeRange openSlot = TimeRange.fromStartEnd(slotStart, change.point(), false);
                if (openSlot.duration() >= request.getDuration())
                    openings.add(openSlot);
                slotStart = -1;
            } else if (slotStart < 0 && occupied.isEmpty()) {
                slotStart = change.point();
            }
        }

        // Add end-of-day slot, if applicable
        if (slotStart >= 0) {
            TimeRange openSlot = TimeRange.fromStartEnd(slotStart, TimeRange.END_OF_DAY, true);
            if (openSlot.duration() >= request.getDuration())
                openings.add(openSlot);
        }

        return openings;
    }
}
