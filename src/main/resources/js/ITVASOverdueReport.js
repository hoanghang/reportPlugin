var ITVASOverdueReport = ITVASOverdueReport || {};

ITVASOverdueReport.validation = function () {
    var check = true;
    var toDate = AJS.$("#txtEndDate").val();
    if (toDate == undefined || toDate == null || toDate == "") {
        AJS.$("#todate-validate").html('');
            AJS
                .$("#todate-validate")
                .append(
                    'Please enter a valid date!');
            check = false;
    }else{
        if (!moment(toDate, "D/MMM/YY", true).isValid()) {
            AJS.$("#todate-validate").html('');
            AJS
                .$("#todate-validate")
                .append(
                    'Please enter a valid date: D/MMM/YY!');
            check = false;
        }
    }

    return check;
}

ITVASOverdueReport.updateProjectsByCategories = function(){
    var projectCategory = AJS.$("#projectCat").val();

    var projectCategories = "";
    if (projectCategory == undefined || projectCategory == null || projectCategory == "") {
        projectCategories = "all";
    } else {
        for (var i = 0; i < projectCategory.length; i++) {
            if (i == 0) {
                projectCategories += projectCategory[i];
            } else {
                projectCategories += "," + projectCategory[i];
            }
        }
    }
    console.log("hang test");
    console.log(projectCategories);
    AJS.$.ajax({
        url: contextPath
        + "/rest/vnpt-report/1.0/report/search/getProjectsByProjectCategories/" + projectCategories,
        type: "GET",
        dataType: "json",
        success: function (data) {
            AJS.$("#projects").html('');
            var temp = "";
            for (var i = 0; i < data.length; i++) {
                temp += "<option value='" + data[i].key + "'>"
                    + data[i].name + "</option>";
            }
            AJS.$("#projects").auiSelect2("destroy");
            AJS.$("#projects").append(temp);
            AJS.$("#projects").auiSelect2();
        },
    });
}