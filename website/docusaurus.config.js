// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const { themes } = require('prism-react-renderer');

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'LionWeb Java',
  tagline: 'Java implementation of the LionWeb specification',
  favicon: 'img/favicon.ico',

  // Set the production url of your site here
  url: 'https://lionweb-io.github.io',
  // Set the /<baseUrl>/ pathname under which your site is served
  baseUrl: '/',

  // GitHub pages deployment config.
  organizationName: 'lionweb-io',
  projectName: 'lionweb-java',

  onBrokenLinks: 'warn',
  onBrokenMarkdownLinks: 'warn',

  // Even if you don't use internalization, this is a required field.
  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      {
        docs: {
          sidebarPath: require.resolve('./sidebars.js'),
          // Please change this to your repo.
          editUrl: 'https://github.com/lionweb-io/lionweb-java/tree/main/docs/',
          routeBasePath: '/', // Set docs as the root
        },
        blog: false,
        pages: false,
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      },
    ],
  ],

  themeConfig: {
    // Replace with your project's social card
    image: 'img/docusaurus-social-card.jpg',
    navbar: {
      title: 'LionWeb Java',
      logo: {
        alt: 'LionWeb Logo',
        src: 'img/lionweb-logo.png',
      },
      items: [
        {
          type: 'docSidebar',
          sidebarId: 'tutorialSidebar',
          position: 'left',
          label: 'Docs',
        },
        {
          href: 'https://github.com/lionweb-io/lionweb-java',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Docs',
          items: [
            {
              label: 'Getting Started',
              to: '/intro',
            },
            {
              label: 'Architecture',
              to: '/architecture/overview',
            },
          ],
        },
        {
          title: 'Community',
          items: [
            {
              label: 'GitHub',
              href: 'https://github.com/lionweb-io/lionweb-java',
            },
            {
              label: 'LionWeb',
              href: 'https://lionweb.io',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} LionWeb. Built with Docusaurus.`,
    },
    prism: {
      theme: themes.github,
      darkTheme: themes.dracula,
      additionalLanguages: ['java', 'groovy'],
    },
  },
};

module.exports = config; 