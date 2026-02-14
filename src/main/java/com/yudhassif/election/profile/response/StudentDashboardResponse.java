package com.yudhassif.election.profile.response;

public record StudentDashboardResponse(
        String firstName,
        String regNumber,
        boolean activated,
        long activeElections
) {
   
}





















//
//public record StudentDashboardResponseIm(
//        String firstName,
//        String lastName
//) implements DashboardResponse {
//
//    @Override
//    public String getRole() {
//        return "STUDENT";
//    }
//
//    @Override
//    public String getFirstName() {
//        return firstName;
//    }
//}
