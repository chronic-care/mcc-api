/*Copyright 2021 Cognitive Medical Systems*/
package com.cognitive.nih.niddk.mccapi.data;

import com.cognitive.nih.niddk.mccapi.data.primative.FuzzyDate;
import com.cognitive.nih.niddk.mccapi.data.primative.MccCodeableConcept;
import com.cognitive.nih.niddk.mccapi.util.FHIRHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hl7.fhir.r4.model.CodeableConcept;

import javax.validation.constraints.NotBlank;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ConditionHistory implements Comparable<ConditionHistory> {
    private static final String dateFormat = "MM/dd/yyyy";
    private static final SimpleDateFormat fmtDate = new SimpleDateFormat(dateFormat);

    @NotBlank
    private MccCodeableConcept code;
    private String onset;
    private String abatement;
    @NotBlank
    private String FHIRid;
    @NotBlank
    private String clinicalStatus;
    @NotBlank
    private String verificationStatus;
    private List<CodeableConcept> categories;

    private FuzzyDate onsetDate;
    private FuzzyDate abatementDate;
    private Date recorded;
    private String note;

    public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@Override
    public int compareTo(ConditionHistory o) {
        if (onsetDate != null && o.onsetDate != null)
        {
            int r = onsetDate.compareTo(o.onsetDate);

            if (r == 0)
            {
                if (abatementDate != null && o.abatementDate != null)
                {
                    return abatementDate.compareTo(o.abatementDate);
                }
                if (abatementDate == null)
                {
                    return 1;
                }
                return -1;
            }
            return r;

        }
        if (onsetDate != null)
        {
            //The onset of the first is not known - sort it first
            return 1;
        }
        else if (o.onsetDate != null)
        {

            return -1;
        }
        else
        {
            // Niether hase a condtion date so we will use the recorded dates
            if (recorded != null & o.recorded!=null)
            {
                return recorded.compareTo(o.recorded);
            }
            if (recorded != null){
                return 1;
            }
            else
            {
                return -1;
            }
        }

    }

    public Date getRecorded() { return recorded;}

    public void setRecorded(Date d) { recorded = d;}

    public String getRecordedAsText() { return recorded == null?"":fmtDate.format(recorded);}

    public String getOnset() {
        return onset;
    }

    public void setOnset(String onset) {
        this.onset = onset;
    }

    public String getAbatement() {
        return abatement;
    }

    public void setAbatement(String abatement) {
        this.abatement = abatement;
    }

    public String getFHIRid() {
        return FHIRid;
    }

    public void setFHIRid(String FHIRid) {
        this.FHIRid = FHIRid;
    }

    public String getClinicalStatus() {
        return clinicalStatus;
    }

    public void setClinicalStatus(String clinicalStatus) {
        this.clinicalStatus = clinicalStatus;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }
    @JsonIgnore
    public FuzzyDate getOnsetDate() {
        return onsetDate;
    }

    @JsonIgnore
    public void setOnsetDate(FuzzyDate onsetDate) {
        this.onsetDate = onsetDate;
    }

    @JsonIgnore
    public FuzzyDate getAbatementDate() {
        return abatementDate;
    }

    @JsonIgnore
    public void setAbatementDate(FuzzyDate abatementDate) {
        this.abatementDate = abatementDate;
    }

    @JsonIgnore
    public List<CodeableConcept> getCategoriesList() {
        return categories;
    }

    /*
    public CodeableConcept getCategory()
    {
        return categories.get(0);

    }
    */
    public String getCategories()
    {
        return FHIRHelper.getConceptsAsDisplayString(categories);
    }

    @JsonIgnore
    public void setCategoriesList(List<CodeableConcept> categories) {
        this.categories = categories;
    }

    public MccCodeableConcept getCode() {
        return code;
    }

    public void setCode(MccCodeableConcept code) {
        this.code = code;
    }
}
