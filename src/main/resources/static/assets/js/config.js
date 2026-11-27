const BASE_URL = 'http://localhost:8080/api';

export const API_ENDPOINTS = {
    STUDENTS: `${BASE_URL}/students`,
    TUITION_FEES: `${BASE_URL}/tuition-fees`,
    STUDENT_TUITIONS: `${BASE_URL}/student-tuitions`,
    PAYMENTS: `${BASE_URL}/payments`,
    TUITION_REPORTS: `${BASE_URL}/tuition-reports`,
    TUITION_LOOKUPS: `${BASE_URL}/tuition-lookups`,
};

export const DEFAULT_USER_ID = '8e12d3e1-1034-4142-bf7f-0a541d6abb20';

export function buildHeaders(extra = {}) {
    return {
        'X-User-Id': DEFAULT_USER_ID,
        ...extra,
    };
}
