export type FeedbackCategory =
  | "Bug"
  | "Gameplay"
  | "Performance"
  | "Ads"
  | "Suggestion"
  | "Other";

export interface Feedback {
  id?: string;
  testerEmail: string;
  testerName?: string;
  rating: number;
  category: FeedbackCategory;
  comment: string;
  createdAt?: TimestampData;
}

export interface TimestampData {
  seconds: number;
  nanoseconds: number;
}
