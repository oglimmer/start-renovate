<template>
  <div class="bg-gradient-to-br from-blue-50 to-indigo-100 py-12 px-4 sm:px-6 lg:px-8">
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
        <h1 class="text-4xl font-bold text-gray-900 mb-2">Imprint / <em>Impressum</em></h1>
        <p class="text-gray-600">Legal information and contact details.</p>
      </div>

      <!-- Content -->
      <section class="bg-white shadow-xl rounded-lg p-8 legal-prose">
        <p>For Germany: Angaben gemäß § 5 DDG (Digitale-Dienste-Gesetz)</p>

        <h2>Diensteanbieter / Website Provider</h2>
        <p>Diese Webseite wird bereitgestellt von / This website is provided by:</p>

        <h3>Oliver Zimpasser</h3>
        <p>
          Berliner Str. 39a<br>
          63110 Rodgau<br>
          Germany
        </p>

        <h2>Kontakt / Contact</h2>
        <p>
          E&#8209;Mail: <a href="mailto:oglimmer@gmail.com">oglimmer@gmail.com</a><br>
          (Angaben zur schnellen elektronischen Kontaktaufnahme und unmittelbaren Kommunikation
          gem. § 5 Abs. 1 Nr. 2 DDG)
        </p>

        <h2>Verantwortlich i.S.d. § 18 Abs. 2 MStV</h2>
        <p>
          Verantwortlich für den Inhalt / Responsible for content:<br>
          Oliver Zimpasser, Berliner Str. 39a, 63110 Rodgau, Germany
        </p>

        <h2>EU&#8209;Streitschlichtung / Dispute Resolution</h2>
        <p>
          Die Europäische Kommission stellt eine Plattform zur Online&#8209;Streitbeilegung (OS) bereit:
          <a href="https://ec.europa.eu/consumers/odr" target="_blank" rel="noopener">https://ec.europa.eu/consumers/odr</a>.
          Wir sind nicht verpflichtet und grundsätzlich nicht bereit, an Streitbeilegungsverfahren vor
          einer Verbraucherschlichtungsstelle teilzunehmen.
        </p>

        <h2>Service Information</h2>
        <p>
          Renovate Initializr is a free, open&#8209;source tool provided at no charge. It is not affiliated
          with or endorsed by Mend.io, the maintainers of Renovate.
        </p>
      </section>

      <!-- Nav -->
      <nav class="mt-6 text-center text-sm text-gray-500 space-x-2">
        <NuxtLink to="/" class="text-indigo-600 hover:text-indigo-800 underline">← Back to home</NuxtLink>
        <span aria-hidden="true">·</span>
        <NuxtLink to="/privacy" class="text-indigo-600 hover:text-indigo-800 underline">Privacy</NuxtLink>
        <span aria-hidden="true">·</span>
        <NuxtLink to="/terms" class="text-indigo-600 hover:text-indigo-800 underline">Terms</NuxtLink>
      </nav>
    </div>
  </div>
</template>

<script setup lang="ts">
useHead({ title: 'Imprint — Renovate Initializr' })
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
