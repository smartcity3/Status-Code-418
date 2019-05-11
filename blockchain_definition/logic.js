'use strict';

/**
 *
 * @param {gr.municipality.mercury.review_issue} review_candidate - review transaction
 * @transaction
 */
async function ReviewIssue(review_candidate) {
    review_candidate.issue.status = 'Under_Review';
    review_candidate.issue.reviewed_by = review_candidate.reviewer;
    let assetRegistry = await getAssetRegistry('gr.municipality.mercury.Issue');
    await assetRegistry.update(review_candidate.issue);
}

/**
 *
 * @param {gr.municipality.mercury.assign_issue} assignment_candidate - issue assignment transaction
 * @transaction
 */
async function AssignIssue(assignment_candidate) {
    assignment_candidate.issue.status = 'Assigned';
    assignment_candidate.issue.assigned_to = assignment_candidate.technician;
    let assetRegistry = await getAssetRegistry('gr.municipality.mercury.Issue');
    await assetRegistry.update(assignment_candidate.issue);
}

/**
 *
 * @param {gr.municipality.mercury.submit_for_review} candidate_fix - fix review submission transaction
 * @transaction
 */
async function SubmitForReview(candidate_fix) {
    candidate_fix.issue.status = 'Fix_Under_Review';
    let assetRegistry = await getAssetRegistry('gr.municipality.mercury.Issue');
    await assetRegistry.update(candidate_fix.issue);
}
/**
 *
 * @param {gr.municipality.mercury.close_issue} issue_to_close - close transaction
 * @transaction
 */
async function CloseIssue(issue_to_close) {
    issue_to_close.issue.status = 'Closed';
    issue_to_close.issue.closed_by = issue_to_close.reviewer;
    let assetRegistry = await getAssetRegistry('gr.municipality.mercury.Issue');
    await assetRegistry.update(issue_to_close.issue);
}

 /**
 *
 * @param {gr.municipality.mercury.volunteer_claim} issue_to_claim - claim transaction
 * @transaction
 */
async function ClaimIssue(volunteer_claim) {
    issue_to_claim.issue.status = 'Assigned_To_Volunteer';
    issue_to_claim.issue.assigned_to_volunteer = issue_to_claim.volunteer;
    let assetRegistry = await getAssetRegistry('gr.municipality.mercury.Issue');
    await assetRegistry.update(issue_to_claim.issue);
}
 
/**
 * 
 * @param {gr.municipality.mercury.volunteer_close} issue_to_close - close transaction
 * @transaction
 */
async function VolunteerCloseIssue(issue_to_close) {
    issue_to_close.issue.status = 'Closed';
    issue_to_close.issue.closed_by = issue_to_close.volunteer;
    let assetRegistry = await getAssetRegistry('gr.municipality.mercury.Issue');
    await assetRegistry.update(issue_to_close.issue);
}

/**
 *
 * @param {gr.municipality.mercury.update_description} issue_description - close transaction
 * @transaction
 */
async function VolunteerCloseIssue(issue_description) {
    issue_description.issue.comments = issue_description.newcomment;
    let assetRegistry = await getAssetRegistry('gr.municipality.mercury.Issue');
    await assetRegistry.update(issue_description.issue);
}