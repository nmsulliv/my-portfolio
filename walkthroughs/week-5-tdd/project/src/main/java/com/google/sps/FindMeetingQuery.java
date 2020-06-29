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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Set;


public final class FindMeetingQuery {
  /**
   * Returns the list of times the meeting could occur that day.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<TimeRange> possibleMeetingTimes = new ArrayList<>();

    // If there is an invalid duration or no events, return times immediately.
    long duration = request.getDuration();
    if (duration > TimeRange.WHOLE_DAY.duration()) {
      return possibleMeetingTimes;
    } else if (events.isEmpty()) {
      possibleMeetingTimes.add(TimeRange.WHOLE_DAY);
      return possibleMeetingTimes;       
    } else {
    }

    // Add the unavailable times of the atteendees requested to a Collection.
    List<TimeRange> unavailableTimes = new ArrayList<>();
    Collection<String> attendeesRequested = request.getAttendees();
    Set<String> eventAttendees;
    for (Event event : events) { 
      eventAttendees = event.getAttendees();
      for (String attendee : eventAttendees) {
        if (attendeesRequested.contains(attendee)) {
          unavailableTimes.add(event.getWhen());
        }
      }
    }

    // If there are no unavailable times, return the whole day as a time range.
    if (unavailableTimes.isEmpty()) {
      possibleMeetingTimes.add(TimeRange.WHOLE_DAY);
      return possibleMeetingTimes;  
    }

    // Create a bit set and add all the unavailable times ranges.
    BitSet available = new BitSet(TimeRange.WHOLE_DAY.duration());
    for (TimeRange range : unavailableTimes) {
      available.set(range.start() + 1, (range.end()));
    }

    // Reverse the bitset so it only has available minutes.
    available.flip(0,TimeRange.WHOLE_DAY.duration() + 1);

    // For each of the available times, find the range and add it to the
    // list of possible meeting times.
    int rangeLength = 0;
    for (int i= 0; i < available.length(); i++) {
      if ((available.get(i)) == true) {
        if ((available.nextSetBit(i + 1)) == (available.nextSetBit(i) + 1)) {
          rangeLength++;
        } else {
          if ((rangeLength + 1) >= duration) {
            TimeRange possibleTime = TimeRange.fromEndDuration(available.nextSetBit(i), rangeLength);
            possibleMeetingTimes.add(possibleTime);
            rangeLength = 0;
          }
        }
      }
    }

    return possibleMeetingTimes;
  }
}
