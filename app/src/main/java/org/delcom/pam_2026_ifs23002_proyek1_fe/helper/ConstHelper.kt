package org.delcom.pam_2026_ifs23002_proyek1_fe.helper

class ConstHelper {
    // Route Names
    enum class RouteNames(val path: String) {
        AuthLogin(path = "auth/login"),
        AuthRegister(path = "auth/register"),

        Home(path = "home"),

        Profile(path = "profile"),
        Ethnographies(path = "ethnographies"),
        EthnographiesAdd(path = "ethnographies/add"),
        EthnographiesDetail(path = "ethnographies/{id}"),
        EthnographiesEdit(path = "ethnographies/{id}/edit"),
    }
}