import type { EmailTemplate } from "./emailLog";

export interface StoredTemplate {
  name: string;
  subject: string;
  htmlBody: string;
  isCustom: boolean;
  createdAt?: { seconds: number };
  updatedAt?: { seconds: number };
}

export interface TemplateWithSource extends StoredTemplate {
  templateKey: EmailTemplate | string;
}
