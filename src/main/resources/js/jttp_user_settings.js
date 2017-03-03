/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

window.everit = window.everit || {};
everit.jttp = everit.jttp || {}; 
everit.jttp.user_settings = everit.jttp.user_settings || {}; //TODO FIXME

(function(jttp, jQuery) {

  jttp.beforeSubmit = function(){
    var userSettingsValues = getUserSettingsValuesJson();
    var json = JSON.stringify(userSettingsValues);
    var userSettingsValuesJson = jQuery('#userSettingsValuesJson');
    userSettingsValuesJson.val(json);
    
    return true;
  }

  function getUserSettingsValuesJson(){
    //text fields
    var startTime = jQuery('#startTime').val() || "";
    var endTime = jQuery('#endTime').val() || "";
    var defaultStartTime = jQuery('#defaultStartTime').val() || "";
    //radio buttons
    var currentOrLast = jQuery('input[name="currentOrLast"]:checked').val();
    var isActualDate  = false;
    if(currentOrLast == "current"){
      isActualDate = true;
    }
    var progressInd = jQuery('input[name="progressInd"]:checked').val();
    var progressIndDaily  = false;
    if(progressInd == "daily"){
      progressIndDaily = true;
    }
    var activeField = jQuery('input[name="activeField"]:checked').val();
    var activeFieldDuration  = false;
    if(activeField == "duration"){
      activeFieldDuration = true;
    }
    var isShowIssueSummaryChecked = jQuery('input[name="isShowIssueSummary"]:checked').val();
    var isShowIssueSummary  = false;
    if(isShowIssueSummaryChecked == "showIssueSummary"){
      isShowIssueSummary = true;
    }
    //check buttons
    var isColoringChecked =  jQuery('input[name="isColoring"]:checked').val();
    var isColoring = false
    if(isColoringChecked != null){
      isColoring = true;
    }
    var isShowFutureLogWarningChecked =  jQuery('input[name="isShowFutureLogWarning"]:checked').val();
    var isShowFutureLogWarning = false
    if(isShowFutureLogWarningChecked != null){
      isShowFutureLogWarning = true;
    }
    var showRemaningEstimateChecked =  jQuery('input[name="showRemaningEstimate"]:checked').val();
    var showRemaningEstimate = false
    if(showRemaningEstimateChecked != null){
      showRemaningEstimate = true;
    }
    var showPeriodWorklogsChecked =  jQuery('input[name="showPeriodWorklogs"]:checked').val();
    var showPeriodWorklogs = false
    if(showPeriodWorklogsChecked != null){
      showPeriodWorklogs = true;
    }
    var showPeriodWorklogsChecked =  jQuery('input[name="showPeriodWorklogs"]:checked').val();
    var showPeriodWorklogs = false
    if(showPeriodWorklogsChecked != null){
      showPeriodWorklogs = true;
    }
    var isRoundedChecked =  jQuery('input[name="isRounded"]:checked').val();
    var isRounded = false
    if(isRoundedChecked != null){
      isRounded = true;
    }
    
    var userSettingsValues = {
      "activeFieldDuration": activeFieldDuration,
      "defaultStartTime": defaultStartTime,
      "endTime": endTime,
      "actualDate": isActualDate,
      "coloring": isColoring,
      "rounded": isRounded,
      "showFutureLogWarning": isShowFutureLogWarning,
      "showIssueSummary": isShowIssueSummary,
      "progressIndDaily": progressIndDaily,
      "showPeriodWorklogs": showPeriodWorklogs,
      "showRemaningEstimate": showRemaningEstimate,
      "startTime": startTime,
    }
    return userSettingsValues;
  }
  
  
})(everit.jttp.user_settings, jQuery);