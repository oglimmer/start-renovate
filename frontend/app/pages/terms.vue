<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-3xl mx-auto">
      <!-- Header -->
      <div class="mb-8">
        <NuxtLink to="/" class="inline-flex items-center text-sm text-indigo-600 hover:text-indigo-800 mb-4">
          <svg class="h-4 w-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
          Back to the configurator
        </NuxtLink>
        <p class="text-xs font-semibold uppercase tracking-wide text-indigo-600 mb-1">Legal</p>
        <h1 class="text-4xl font-bold text-gray-900 mb-2">Terms &amp; Conditions</h1>
        <p class="text-gray-600">Last updated: {{ lastUpdated }}</p>
      </div>

      <!-- Content -->
      <section class="bg-white shadow-xl rounded-lg p-8 legal-prose">
        <p>
          These Terms and Conditions ("Terms") govern your use of Renovate Initializr ("the Service"),
          provided by oglimmer.com / Oliver Zimpasser ("we", "our", or "us"). By accessing or using the
          Service, you agree to abide by these Terms. If you do not agree with any part of these Terms,
          you should not use the Service.
        </p>

        <h2>1. The Service</h2>
        <p>
          1.1. Renovate Initializr is a free tool that helps you build, review and download a
          <code>renovate.json</code> configuration file. It offers a guided configuration builder, an
          optional AI&#8209;powered review of a configuration, a public REST API for the same review, and an
          optional dashboard that compares the Renovate configuration of repositories you connect from a
          supported Git host.
        </p>
        <p>
          1.2. Renovate Initializr is an independent project. It is not affiliated with, sponsored by, or
          endorsed by Mend.io or the Renovate project. "Renovate" is used only to describe the
          configuration files this tool produces.
        </p>

        <h2>2. Acceptable Use</h2>
        <p>2.1. You must be at least 16 years old to use the Service.</p>
        <p>
          2.2. You may not use the Service for any unauthorized or illegal purpose, including but not
          limited to distributing malware, spamming, scraping, attempting to gain unauthorized access to
          accounts or repositories, or attempting to disrupt or overload the Service or its underlying
          AI provider.
        </p>
        <p>
          2.3. You are responsible for the content you submit to the Service, including the configuration
          text you paste in for review. You must not submit content that is illegal, infringing, or that
          contains secrets (for example tokens, passwords or private keys); a <code>renovate.json</code>
          should not contain such material.
        </p>

        <h2>3. Accounts and Repository Access</h2>
        <p>
          3.1. The configuration builder, the live preview, the download and the public REST API can be
          used without an account.
        </p>
        <p>
          3.2. The dashboard requires you to sign in through a supported OAuth provider (currently GitHub
          and/or GitLab). When you sign in, you authorize the Service to use your provider access token to
          list your repositories and read their Renovate configuration on your behalf. The Service requests
          the access scopes its repository features rely on (the GitHub <code>repo</code> and
          <code>read:org</code> scopes, or the GitLab <code>read_api</code> scope). Although these tokens may
          technically permit more, the Service only reads repository metadata and Renovate configuration and
          never creates, modifies or deletes anything in your repositories.
        </p>
        <p>
          3.3. You are responsible for maintaining the security of the account you use to sign in and for
          all activity that occurs under your session. You can revoke the Service's access at any time from
          your provider's settings (GitHub/GitLab authorized applications).
        </p>
        <p>
          3.4. We reserve the right to suspend or terminate access at our discretion if we believe you have
          violated these Terms or engaged in fraudulent or abusive behavior.
        </p>

        <h2>4. Intellectual Property</h2>
        <p>
          4.1. You retain all rights in the configuration content you submit. We claim no ownership of it.
        </p>
        <p>
          4.2. By submitting a configuration for AI review, you grant us a limited, non&#8209;exclusive,
          royalty&#8209;free licence to process and transmit that content solely to perform the requested
          review, including transmitting it to our third&#8209;party AI provider for that single request (see
          Section 6). We do not use your submitted content for advertising, marketing, or training of
          machine&#8209;learning models.
        </p>
        <p>
          4.3. The generated <code>renovate.json</code> output is yours to use freely. It is provided
          without warranty as to fitness for any particular repository or workflow (see Section 8).
        </p>

        <h2>5. Privacy</h2>
        <p>
          5.1. Our <NuxtLink to="/privacy">Privacy Policy</NuxtLink> explains how we collect and process
          personal data, the purposes and legal bases, retention periods, and your rights. By using the
          Service, you acknowledge that you have read it.
        </p>

        <h2>6. AI-Assisted Review</h2>
        <p>
          6.1. The "AI feedback" feature and the REST API send the configuration text you provide to a
          third&#8209;party AI provider (DeepSeek) to generate a plain&#8209;language summary, a list of issues, and
          an improved configuration. This feature is only active when the operator of the instance has
          configured an AI provider API key.
        </p>
        <p>
          6.2. AI&#8209;generated suggestions are provided "as is". They may be incomplete or incorrect. You are
          solely responsible for reviewing the output and deciding whether to apply it to your repository.
        </p>

        <h2>7. Service Availability and Changes</h2>
        <p>
          7.1. The Service is provided free of charge and on a best&#8209;effort basis. We reserve the right to
          suspend, terminate, modify, or impose usage limits on the Service at any time, with or without
          notice.
        </p>

        <h2>8. Disclaimer of Warranties</h2>
        <p>
          8.1. The Service is provided "as is" and "as available". We make no warranties or representations
          regarding the accuracy, completeness, reliability, or fitness for a particular purpose of the
          Service, the generated configuration, or any AI&#8209;generated output. Applying a generated or suggested
          configuration to your repository is at your own risk.
        </p>

        <h2>9. Limitation of Liability</h2>
        <p>
          9.1. To the extent permitted by applicable law, we shall not be liable for any indirect,
          incidental, special, consequential, or punitive damages, or any loss of profits, revenues, data,
          or goodwill, arising out of or in connection with your use of the Service — including, without
          limitation, any consequences of applying a configuration produced or suggested by the Service.
        </p>

        <h2>10. Governing Law</h2>
        <p>
          10.1. These Terms shall be governed by and construed in accordance with the laws of Germany,
          without regard to its conflict of law principles.
        </p>

        <h2>11. Changes to Terms</h2>
        <p>
          11.1. We reserve the right to modify or replace these Terms at any time. It is your responsibility
          to review these Terms periodically. Your continued use of the Service after any changes indicates
          your acceptance of those changes.
        </p>

        <h2>12. Contact</h2>
        <p>
          12.1. If you have any questions or concerns about these Terms, please contact us at
          <a href="mailto:oglimmer@gmail.com">oglimmer@gmail.com</a>.
        </p>
      </section>

      <!-- Nav -->
      <nav class="mt-6 text-center text-sm text-gray-500 space-x-2">
        <NuxtLink to="/" class="text-indigo-600 hover:text-indigo-800 underline">← Back to home</NuxtLink>
        <span aria-hidden="true">·</span>
        <NuxtLink to="/privacy" class="text-indigo-600 hover:text-indigo-800 underline">Privacy</NuxtLink>
        <span aria-hidden="true">·</span>
        <NuxtLink to="/imprint" class="text-indigo-600 hover:text-indigo-800 underline">Imprint</NuxtLink>
      </nav>
    </div>
  </div>
</template>

<script setup lang="ts">
useHead({ title: 'Terms & Conditions — Renovate Initializr' })
const lastUpdated = '2026-06-21'
</script>

<style scoped>
.legal-prose :deep(h2) {
  @apply text-xl font-semibold text-gray-900 mt-8 mb-3;
}
.legal-prose :deep(h3) {
  @apply text-base font-semibold text-gray-900 mt-4 mb-1;
}
.legal-prose :deep(p) {
  @apply text-gray-700 leading-relaxed mb-4;
}
.legal-prose :deep(a) {
  @apply text-indigo-600 hover:text-indigo-800 underline;
}
.legal-prose :deep(ul) {
  @apply list-disc pl-6 space-y-2 text-gray-700 mb-4;
}
.legal-prose :deep(code) {
  @apply bg-gray-100 px-1.5 py-0.5 rounded text-sm;
}
</style>
