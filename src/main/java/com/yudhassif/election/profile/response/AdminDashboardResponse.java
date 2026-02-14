package com.yudhassif.election.profile.response;

public record AdminDashboardResponse(
        String firstName,
        long totalEnabledStudents,
        long totalStudents,
        long totalElections,
        long activeElections
) {}













//package com.yudhassif.election.profile.response;
//
//public record AdminDashboardImpl(
//        String firstName,
//        long totalEnabledStudents,
//        Long totalStudents,
//        long totalElections,
//        long activeElections,
//        String role
//) implements DashboardResponse {
//    public AdminDashboardImpl(
//            String firstName,
//            long totalEnabledStudents,
//            Long totalStudents,
//            long totalElections,
//            long activeElections
//    ) {
//        this(firstName, totalEnabledStudents, totalStudents, totalElections, activeElections, "ADMIN");
//    }
//
//
//    @Override
//    public String getRole() {
//        return "ADMIN";
//    }
//
//    @Override
//    public String getFirstName() {
//        return firstName;
//    }
//}
